import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * SWYNEX Technologies - Java Internship Program
 * Task 1: Java Fundamentals Application
 *
 * Project: LifeOS - Personal Life Manager (Console Application)
 *
 * Core Java Concepts Demonstrated:
 * 1. Classes, Objects & Enums: LifeTask, Habit, FinanceEntry, WellnessLog, Priority, TransactionType, LifeOSService
 * 2. Encapsulation & Methods: Private state, getters/setters, domain calculation methods, and LifeOS Score engine
 * 3. Java Collections Framework: LinkedHashMap, ArrayList, TreeSet, Streams, and Comparators
 * 4. Exception Handling: Custom domain exceptions + try-catch validation for numbers and dates
 * 5. Clean Code Structure: Modular separation of Exceptions, Models, Service Layer, and Console Controller
 */
public class task1 {

    // =========================================================================
    // 1. CUSTOM EXCEPTIONS (Exception Handling)
    // =========================================================================

    /**
     * Thrown when a requested Task ID or Habit ID does not exist.
     */
    static class EntityNotFoundException extends Exception {
        public EntityNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when user input violates domain constraints (negative amounts, invalid ratings, blank text).
     */
    static class InvalidLifeDataException extends Exception {
        public InvalidLifeDataException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when an expense pushes monthly spending past the configured budget limit.
     */
    static class BudgetLimitWarningException extends Exception {
        public BudgetLimitWarningException(String message) {
            super(message);
        }
    }

    // =========================================================================
    // 2. ENUMS & MODEL CLASSES (Classes, Objects & Encapsulation)
    // =========================================================================

    enum Priority {
        HIGH(3), MEDIUM(2), LOW(1);

        private final int weight;

        Priority(int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }
    }

    enum TransactionType {
        INCOME, EXPENSE
    }

    /**
     * Represents an actionable personal task or goal.
     */
    static class LifeTask {
        private final String taskId;
        private String title;
        private String category;
        private Priority priority;
        private LocalDate dueDate;
        private boolean completed;

        public LifeTask(String taskId, String title, String category, Priority priority, LocalDate dueDate) {
            this.taskId = taskId.toUpperCase().trim();
            this.title = title.trim();
            this.category = category.trim();
            this.priority = priority;
            this.dueDate = dueDate;
            this.completed = false;
        }

        public String getTaskId() {
            return taskId;
        }

        public String getTitle() {
            return title;
        }

        public String getCategory() {
            return category;
        }

        public Priority getPriority() {
            return priority;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void markCompleted() {
            this.completed = true;
        }

        public String getStatusLabel() {
            if (completed) {
                return "COMPLETED";
            }
            if (dueDate.isBefore(LocalDate.now())) {
                return "OVERDUE";
            }
            return "PENDING";
        }

        @Override
        public String toString() {
            return String.format("| %-7s | %-26s | %-12s | %-8s | %-10s | %-10s |",
                    taskId,
                    truncate(title, 26),
                    truncate(category, 12),
                    priority,
                    dueDate,
                    getStatusLabel());
        }
    }

    /**
     * Represents a recurring daily habit with streak tracking.
     */
    static class Habit {
        private final String habitId;
        private final String name;
        private final String cue;
        private int currentStreak;
        private int bestStreak;
        private int totalCheckIns;
        private LocalDate lastCompletedDate;

        public Habit(String habitId, String name, String cue, int currentStreak, int bestStreak, int totalCheckIns) {
            this.habitId = habitId.toUpperCase().trim();
            this.name = name.trim();
            this.cue = cue.trim();
            this.currentStreak = currentStreak;
            this.bestStreak = Math.max(currentStreak, bestStreak);
            this.totalCheckIns = totalCheckIns;
            this.lastCompletedDate = currentStreak > 0 ? LocalDate.now().minusDays(1) : null;
        }

        public String getHabitId() {
            return habitId;
        }

        public String getName() {
            return name;
        }

        public int getCurrentStreak() {
            return currentStreak;
        }

        public int getBestStreak() {
            return bestStreak;
        }

        public int getTotalCheckIns() {
            return totalCheckIns;
        }

        public boolean checkInToday() {
            LocalDate today = LocalDate.now();
            if (today.equals(lastCompletedDate)) {
                return false; // Already checked in today
            }
            currentStreak++;
            totalCheckIns++;
            if (currentStreak > bestStreak) {
                bestStreak = currentStreak;
            }
            lastCompletedDate = today;
            return true;
        }

        public void resetStreak() {
            this.currentStreak = 0;
        }

        @Override
        public String toString() {
            String statusToday = LocalDate.now().equals(lastCompletedDate) ? "DONE TODAY" : "PENDING";
            return String.format("| %-7s | %-24s | %-16s | %6d days | %6d days | %-10s |",
                    habitId,
                    truncate(name, 24),
                    truncate(cue, 16),
                    currentStreak,
                    bestStreak,
                    statusToday);
        }
    }

    /**
     * Represents a personal finance transaction (Income or Expense).
     */
    static class FinanceEntry {
        private final String entryId;
        private final TransactionType type;
        private final String category;
        private final String description;
        private final double amount;
        private final LocalDateTime timestamp;

        private static final DateTimeFormatter FORMATTER =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        public FinanceEntry(String entryId, TransactionType type, String category, String description, double amount) {
            this.entryId = entryId;
            this.type = type;
            this.category = category.trim();
            this.description = description.trim();
            this.amount = amount;
            this.timestamp = LocalDateTime.now();
        }

        public TransactionType getType() {
            return type;
        }

        public String getCategory() {
            return category;
        }

        public double getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            String sign = (type == TransactionType.INCOME) ? "+" : "-";
            return String.format("| %-7s | %-16s | %-7s | %-12s | %-22s | %sRs. %9.2f |",
                    entryId,
                    timestamp.format(FORMATTER),
                    type,
                    truncate(category, 12),
                    truncate(description, 22),
                    sign,
                    amount);
        }
    }

    /**
     * Represents a daily wellness, sleep, and mood reflection entry.
     */
    static class WellnessLog {
        private final LocalDate date;
        private final int moodRating; // 1 to 5
        private final double sleepHours;
        private final int waterGlasses;
        private final String gratitudeNote;

        public WellnessLog(LocalDate date, int moodRating, double sleepHours, int waterGlasses, String gratitudeNote) {
            this.date = date;
            this.moodRating = moodRating;
            this.sleepHours = sleepHours;
            this.waterGlasses = waterGlasses;
            this.gratitudeNote = gratitudeNote.trim();
        }

        public int getMoodRating() {
            return moodRating;
        }

        public double getSleepHours() {
            return sleepHours;
        }

        public int getWaterGlasses() {
            return waterGlasses;
        }

        @Override
        public String toString() {
            return String.format("| %-10s | %d / 5     | %5.1f hrs  | %4d glasses | %-32s |",
                    date,
                    moodRating,
                    sleepHours,
                    waterGlasses,
                    truncate(gratitudeNote, 32));
        }
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen - 3) + "...";
    }

    // =========================================================================
    // 3. SERVICE LAYER (Collections & Business Logic)
    // =========================================================================

    static class LifeOSService {
        // Collections demonstrating Map, List, and Set usage
        private final Map<String, LifeTask> tasks = new LinkedHashMap<>();
        private final Map<String, Habit> habits = new LinkedHashMap<>();
        private final List<FinanceEntry> ledger = new ArrayList<>();
        private final List<WellnessLog> wellnessLogs = new ArrayList<>();

        private int taskCounter = 100;
        private int habitCounter = 200;
        private int financeCounter = 300;
        private double monthlyExpenseBudget = 15000.00;

        public LifeOSService() {
            seedSampleLifeData();
        }

        /**
         * Pre-loads realistic personal data so the console app is ready to demo immediately.
         */
        private void seedSampleLifeData() {
            try {
                // Seed Tasks
                LifeTask t1 = addTask("Complete SWYNEX Java Task 1", "Career", Priority.HIGH, LocalDate.now().plusDays(1));
                t1.markCompleted();
                addTask("Push LifeOS code to GitHub repo", "Career", Priority.HIGH, LocalDate.now().plusDays(1));
                addTask("30-Minute Evening Running Session", "Health", Priority.MEDIUM, LocalDate.now());
                addTask("Read 15 pages of Effective Java", "Learning", Priority.MEDIUM, LocalDate.now().plusDays(2));
                addTask("Pay monthly broadband bill", "Finance", Priority.LOW, LocalDate.now().plusDays(4));

                // Seed Habits
                habits.put("HAB-201", new Habit("HAB-201", "Solve 2 DSA Problems", "After Morning Coffee", 6, 14, 28));
                habits.put("HAB-202", new Habit("HAB-202", "8 Glasses Hydration", "Desk Water Bottle", 11, 11, 40));
                habits.put("HAB-203", new Habit("HAB-203", "Zero Screen 30m Pre-Bed", "10:30 PM Alarm", 4, 9, 19));
                habitCounter = 203;

                // Seed Finance Entries
                recordFinance(TransactionType.INCOME, "Stipend", "Freelance / Internship Savings", 18000.00);
                recordFinance(TransactionType.EXPENSE, "Education", "Java & Cloud Reference Books", 1450.00);
                recordFinance(TransactionType.EXPENSE, "Food", "Weekly Groceries & Fruit", 2100.00);
                recordFinance(TransactionType.EXPENSE, "Utilities", "High-Speed Fiber Internet", 899.00);

                // Seed Wellness Logs
                wellnessLogs.add(new WellnessLog(LocalDate.now().minusDays(2), 4, 7.5, 8, "Solved a tricky recursion bug"));
                wellnessLogs.add(new WellnessLog(LocalDate.now().minusDays(1), 5, 8.0, 9, "Great workout and productive coding"));
            } catch (Exception ignored) {
                // Seed data is guaranteed valid
            }
        }

        // ------------------- TASK MANAGEMENT METHODS -------------------

        public LifeTask addTask(String title, String category, Priority priority, LocalDate dueDate)
                throws InvalidLifeDataException {
            if (title == null || title.trim().isEmpty()) {
                throw new InvalidLifeDataException("Task title cannot be empty.");
            }
            if (category == null || category.trim().isEmpty()) {
                throw new InvalidLifeDataException("Task category cannot be empty.");
            }
            taskCounter++;
            String id = "TSK-" + taskCounter;
            LifeTask task = new LifeTask(id, title, category, priority, dueDate);
            tasks.put(id, task);
            return task;
        }

        public LifeTask completeTask(String taskId) throws EntityNotFoundException {
            LifeTask task = getTaskById(taskId);
            task.markCompleted();
            return task;
        }

        public LifeTask removeTask(String taskId) throws EntityNotFoundException {
            LifeTask task = getTaskById(taskId);
            tasks.remove(task.getTaskId());
            return task;
        }

        public LifeTask getTaskById(String taskId) throws EntityNotFoundException {
            String key = taskId.toUpperCase().trim();
            LifeTask task = tasks.get(key);
            if (task == null) {
                throw new EntityNotFoundException("No task found with ID: " + key);
            }
            return task;
        }

        public List<LifeTask> getTasksSortedByPriority() {
            return tasks.values().stream()
                    .sorted(Comparator.comparing(LifeTask::isCompleted)
                            .thenComparing((LifeTask t) -> t.getPriority().getWeight(), Comparator.reverseOrder())
                            .thenComparing(LifeTask::getDueDate))
                    .collect(Collectors.toList());
        }

        // ------------------- HABIT TRACKER METHODS -------------------

        public Habit addHabit(String name, String cue) throws InvalidLifeDataException {
            if (name == null || name.trim().isEmpty()) {
                throw new InvalidLifeDataException("Habit name cannot be empty.");
            }
            if (cue == null || cue.trim().isEmpty()) {
                throw new InvalidLifeDataException("Habit trigger/cue cannot be empty.");
            }
            habitCounter++;
            String id = "HAB-" + habitCounter;
            Habit habit = new Habit(id, name, cue, 0, 0, 0);
            habits.put(id, habit);
            return habit;
        }

        public Habit checkInHabit(String habitId) throws EntityNotFoundException, InvalidLifeDataException {
            String key = habitId.toUpperCase().trim();
            Habit habit = habits.get(key);
            if (habit == null) {
                throw new EntityNotFoundException("No habit found with ID: " + key);
            }
            boolean updated = habit.checkInToday();
            if (!updated) {
                throw new InvalidLifeDataException("Habit '" + habit.getName() + "' is already checked in for today!");
            }
            return habit;
        }

        public List<Habit> getAllHabits() {
            return new ArrayList<>(habits.values());
        }

        // ------------------- FINANCE & BUDGET METHODS -------------------

        public FinanceEntry recordFinance(TransactionType type, String category, String description, double amount)
                throws InvalidLifeDataException, BudgetLimitWarningException {
            if (amount <= 0) {
                throw new InvalidLifeDataException("Transaction amount must be greater than zero.");
            }
            if (category == null || category.trim().isEmpty()) {
                throw new InvalidLifeDataException("Category cannot be blank.");
            }
            financeCounter++;
            String id = "FIN-" + financeCounter;
            FinanceEntry entry = new FinanceEntry(id, type, category, description, amount);
            ledger.add(entry);

            if (type == TransactionType.EXPENSE && getTotalExpenses() > monthlyExpenseBudget) {
                throw new BudgetLimitWarningException(String.format(
                        "Expense recorded, but total expenses (Rs. %.2f) now exceed your monthly budget of Rs. %.2f!",
                        getTotalExpenses(), monthlyExpenseBudget));
            }
            return entry;
        }

        public double getTotalIncome() {
            return ledger.stream()
                    .filter(e -> e.getType() == TransactionType.INCOME)
                    .mapToDouble(FinanceEntry::getAmount)
                    .sum();
        }

        public double getTotalExpenses() {
            return ledger.stream()
                    .filter(e -> e.getType() == TransactionType.EXPENSE)
                    .mapToDouble(FinanceEntry::getAmount)
                    .sum();
        }

        public double getNetBalance() {
            return getTotalIncome() - getTotalExpenses();
        }

        public double getMonthlyExpenseBudget() {
            return monthlyExpenseBudget;
        }

        public void setMonthlyExpenseBudget(double newBudget) throws InvalidLifeDataException {
            if (newBudget <= 0) {
                throw new InvalidLifeDataException("Monthly budget must be greater than zero.");
            }
            this.monthlyExpenseBudget = newBudget;
        }

        public List<FinanceEntry> getLedger() {
            return new ArrayList<>(ledger);
        }

        public Set<String> getDistinctLifeCategories() {
            Set<String> categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            tasks.values().forEach(t -> categories.add(t.getCategory()));
            ledger.forEach(f -> categories.add(f.getCategory()));
            return categories;
        }

        // ------------------- WELLNESS JOURNAL METHODS -------------------

        public WellnessLog addWellnessLog(int mood, double sleepHours, int waterGlasses, String gratitudeNote)
                throws InvalidLifeDataException {
            if (mood < 1 || mood > 5) {
                throw new InvalidLifeDataException("Mood rating must be between 1 (Low) and 5 (Great).");
            }
            if (sleepHours < 0 || sleepHours > 24) {
                throw new InvalidLifeDataException("Sleep hours must be between 0 and 24.");
            }
            if (waterGlasses < 0) {
                throw new InvalidLifeDataException("Water intake cannot be negative.");
            }
            WellnessLog log = new WellnessLog(LocalDate.now(), mood, sleepHours, waterGlasses, gratitudeNote);
            wellnessLogs.add(log);
            return log;
        }

        public List<WellnessLog> getWellnessLogs() {
            return new ArrayList<>(wellnessLogs);
        }

        // ------------------- LIFEOS SCORE ENGINE -------------------

        /**
         * Computes a holistic LifeOS Score (0 - 100) across Tasks, Habits, Finance, and Wellness.
         */
        public int calculateLifeOSScore() {
            // 1. Task Score (up to 25 pts)
            long totalTasks = tasks.size();
            long completedTasks = tasks.values().stream().filter(LifeTask::isCompleted).count();
            int taskScore = totalTasks == 0 ? 20 : (int) Math.round((completedTasks * 25.0) / totalTasks);

            // 2. Habit Streak Score (up to 25 pts)
            double avgStreak = habits.values().stream().mapToInt(Habit::getCurrentStreak).average().orElse(0.0);
            int habitScore = (int) Math.min(25, Math.round(avgStreak * 3.5));

            // 3. Financial Health Score (up to 25 pts)
            double expenses = getTotalExpenses();
            int financeScore = 25;
            if (expenses > monthlyExpenseBudget) {
                financeScore = 10;
            } else if (expenses > monthlyExpenseBudget * 0.85) {
                financeScore = 18;
            }

            // 4. Wellness Score (up to 25 pts)
            double avgMood = wellnessLogs.stream().mapToInt(WellnessLog::getMoodRating).average().orElse(4.0);
            int wellnessScore = (int) Math.round((avgMood / 5.0) * 25);

            return Math.min(100, taskScore + habitScore + financeScore + wellnessScore);
        }
    }

    // =========================================================================
    // 4. CONSOLE CONTROLLER & INTERACTIVE MENU
    // =========================================================================

    private static final Scanner scanner = new Scanner(System.in);
    private static final LifeOSService lifeOS = new LifeOSService();
    private static final DateTimeFormatter DATE_INPUT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        printBanner();
        boolean running = true;

        while (running) {
            printMainMenu();
            try {
                int option = readInt("Select a LifeOS module (0-9): ");
                System.out.println();
                switch (option) {
                    case 1:
                        handleLifeOSDashboard();
                        break;
                    case 2:
                        handleViewTasks();
                        break;
                    case 3:
                        handleAddTask();
                        break;
                    case 4:
                        handleCompleteOrDeleteTask();
                        break;
                    case 5:
                        handleHabitTracker();
                        break;
                    case 6:
                        handleFinanceSummary();
                        break;
                    case 7:
                        handleAddFinanceTransaction();
                        break;
                    case 8:
                        handleWellnessJournal();
                        break;
                    case 9:
                        handleUpdateMonthlyBudget();
                        break;
                    case 0:
                        running = false;
                        System.out.println("Saving session... Stay consistent and focused! Exiting LifeOS.");
                        break;
                    default:
                        System.out.println("[!] Invalid option. Please enter a number between 0 and 9.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[INPUT ERROR] Please enter a valid numeric choice.");
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void printBanner() {
        System.out.println("==========================================================================================");
        System.out.println("                         LifeOS - PERSONAL LIFE MANAGER (v1.0)                            ");
        System.out.println("                  Tasks | Habits | Personal Finance | Wellness Dashboard                  ");
        System.out.println("                         SWYNEX Technologies Java Internship                              ");
        System.out.println("==========================================================================================");
    }

    private static void printMainMenu() {
        System.out.println("\n------------------------------------- LifeOS MENU ----------------------------------------");
        System.out.println("  1. View LifeOS Executive Dashboard & Score   6. View Finance Ledger & Budget Status");
        System.out.println("  2. View All Tasks (Sorted by Priority)       7. Log Income or Expense Transaction");
        System.out.println("  3. Add New Personal Task / Goal              8. Wellness & Mood Journal (View / Log)");
        System.out.println("  4. Complete or Remove a Task                 9. Configure Monthly Expense Budget");
        System.out.println("  5. Habit Tracker (View / Check-In / Add)     0. Exit LifeOS");
        System.out.println("------------------------------------------------------------------------------------------");
    }

    // ------------------- MODULE 1: EXECUTIVE DASHBOARD -------------------

    private static void handleLifeOSDashboard() {
        List<LifeTask> allTasks = lifeOS.getTasksSortedByPriority();
        long completedCount = allTasks.stream().filter(LifeTask::isCompleted).count();
        List<Habit> habits = lifeOS.getAllHabits();
        List<WellnessLog> logs = lifeOS.getWellnessLogs();
        double avgSleep = logs.stream().mapToDouble(WellnessLog::getSleepHours).average().orElse(0.0);
        double avgMood = logs.stream().mapToInt(WellnessLog::getMoodRating).average().orElse(0.0);
        int score = lifeOS.calculateLifeOSScore();

        System.out.println("==========================================================================================");
        System.out.println("                              LifeOS EXECUTIVE DASHBOARD                                  ");
        System.out.println("==========================================================================================");
        System.out.printf("  OVERALL LifeOS SCORE    : %d / 100 (%s)%n", score, getScoreTier(score));
        System.out.printf("  Task Completion         : %d / %d tasks completed%n", completedCount, allTasks.size());
        System.out.printf("  Active Habits Tracked   : %d daily habits%n", habits.size());
        System.out.printf("  Net Financial Balance   : Rs. %,.2f (Income: Rs. %,.2f | Expenses: Rs. %,.2f)%n",
                lifeOS.getNetBalance(), lifeOS.getTotalIncome(), lifeOS.getTotalExpenses());
        System.out.printf("  Monthly Budget Status   : Rs. %,.2f / Rs. %,.2f used%n",
                lifeOS.getTotalExpenses(), lifeOS.getMonthlyExpenseBudget());
        System.out.printf("  Wellness Averages       : Mood %.1f/5.0 | Sleep %.1f hrs/night%n", avgMood, avgSleep);
        System.out.printf("  Active Life Categories  : %s%n", String.join(", ", lifeOS.getDistinctLifeCategories()));
        System.out.println("==========================================================================================");
    }

    private static String getScoreTier(int score) {
        if (score >= 85) {
            return "PEAK PERFORMANCE";
        } else if (score >= 70) {
            return "STRONG MOMENTUM";
        } else if (score >= 50) {
            return "BUILDING CONSISTENCY";
        }
        return "NEEDS FOCUS";
    }

    // ------------------- MODULE 2, 3, 4: TASKS & GOALS -------------------

    private static void handleViewTasks() {
        List<LifeTask> tasks = lifeOS.getTasksSortedByPriority();
        if (tasks.isEmpty()) {
            System.out.println("No tasks registered yet.");
            return;
        }
        System.out.println("PERSONAL TASKS & GOALS (SORTED BY PRIORITY & DUE DATE):");
        System.out.println("+---------+----------------------------+--------------+----------+------------+------------+");
        System.out.println("| ID      | TASK TITLE                 | CATEGORY     | PRIORITY | DUE DATE   | STATUS     |");
        System.out.println("+---------+----------------------------+--------------+----------+------------+------------+");
        for (LifeTask task : tasks) {
            System.out.println(task);
        }
        System.out.println("+---------+----------------------------+--------------+----------+------------+------------+");
    }

    private static void handleAddTask() {
        System.out.println("--- ADD NEW PERSONAL TASK ---");
        try {
            String title = readNonEmptyString("Enter Task Title: ");
            String category = readNonEmptyString("Enter Category (e.g., Career, Health, Study, Errands): ");
            String priorityStr = readNonEmptyString("Enter Priority (HIGH / MEDIUM / LOW): ").toUpperCase();
            Priority priority = Priority.valueOf(priorityStr);
            String dateStr = readNonEmptyString("Enter Due Date (YYYY-MM-DD, e.g., " + LocalDate.now().plusDays(3) + "): ");
            LocalDate dueDate = LocalDate.parse(dateStr, DATE_INPUT_FMT);

            LifeTask created = lifeOS.addTask(title, category, priority, dueDate);
            System.out.println("[SUCCESS] Added task '" + created.getTitle() + "' with ID: " + created.getTaskId());
        } catch (IllegalArgumentException e) {
            System.out.println("[VALIDATION ERROR] Priority must be HIGH, MEDIUM, or LOW.");
        } catch (DateTimeParseException e) {
            System.out.println("[DATE ERROR] Invalid date format. Please use YYYY-MM-DD.");
        } catch (InvalidLifeDataException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void handleCompleteOrDeleteTask() {
        handleViewTasks();
        System.out.println("\n1. Mark Task as Completed    2. Delete Task    0. Back");
        try {
            int subChoice = readInt("Choose action: ");
            if (subChoice == 1) {
                String id = readNonEmptyString("Enter Task ID to mark completed (e.g., TSK-102): ");
                LifeTask updated = lifeOS.completeTask(id);
                System.out.println("[SUCCESS] Completed task: " + updated.getTitle());
            } else if (subChoice == 2) {
                String id = readNonEmptyString("Enter Task ID to remove: ");
                LifeTask removed = lifeOS.removeTask(id);
                System.out.println("[SUCCESS] Removed task: " + removed.getTitle());
            }
        } catch (EntityNotFoundException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // ------------------- MODULE 5: HABIT TRACKER -------------------

    private static void handleHabitTracker() {
        printHabitTable();
        System.out.println("\n1. Check-In Habit for Today    2. Create New Daily Habit    0. Back");
        try {
            int sub = readInt("Choose action: ");
            if (sub == 1) {
                String id = readNonEmptyString("Enter Habit ID to check in (e.g., HAB-201): ");
                Habit habit = lifeOS.checkInHabit(id);
                System.out.printf("[STREAK UP!] '%s' checked in! Current streak: %d days (Best: %d days).%n",
                        habit.getName(), habit.getCurrentStreak(), habit.getBestStreak());
            } else if (sub == 2) {
                String name = readNonEmptyString("Enter Habit Name (e.g., Read 10 Pages): ");
                String cue = readNonEmptyString("Enter Daily Trigger/Cue (e.g., After Dinner): ");
                Habit created = lifeOS.addHabit(name, cue);
                System.out.println("[SUCCESS] Created habit '" + created.getName() + "' (" + created.getHabitId() + ").");
            }
        } catch (EntityNotFoundException | InvalidLifeDataException e) {
            System.out.println("[NOTICE] " + e.getMessage());
        }
    }

    private static void printHabitTable() {
        List<Habit> habits = lifeOS.getAllHabits();
        System.out.println("DAILY HABIT & STREAK TRACKER:");
        System.out.println("+---------+--------------------------+------------------+-------------+-------------+------------+");
        System.out.println("| ID      | HABIT NAME               | TRIGGER / CUE    | STREAK      | BEST STREAK | TODAY      |");
        System.out.println("+---------+--------------------------+------------------+-------------+-------------+------------+");
        for (Habit h : habits) {
            System.out.println(h);
        }
        System.out.println("+---------+--------------------------+------------------+-------------+-------------+------------+");
    }

    // ------------------- MODULE 6, 7, 9: PERSONAL FINANCE -------------------

    private static void handleFinanceSummary() {
        List<FinanceEntry> entries = lifeOS.getLedger();
        System.out.println("PERSONAL FINANCE LEDGER:");
        System.out.println("+---------+------------------+---------+--------------+------------------------+---------------+");
        System.out.println("| ID      | TIMESTAMP        | TYPE    | CATEGORY     | DESCRIPTION            | AMOUNT        |");
        System.out.println("+---------+------------------+---------+--------------+------------------------+---------------+");
        for (FinanceEntry entry : entries) {
            System.out.println(entry);
        }
        System.out.println("+---------+------------------+---------+--------------+------------------------+---------------+");
        System.out.printf("  Total Income: Rs. %,.2f  |  Total Expenses: Rs. %,.2f  |  Net Balance: Rs. %,.2f%n",
                lifeOS.getTotalIncome(), lifeOS.getTotalExpenses(), lifeOS.getNetBalance());
        System.out.printf("  Monthly Expense Budget: Rs. %,.2f (Remaining: Rs. %,.2f)%n",
                lifeOS.getMonthlyExpenseBudget(),
                lifeOS.getMonthlyExpenseBudget() - lifeOS.getTotalExpenses());
    }

    private static void handleAddFinanceTransaction() {
        System.out.println("--- LOG INCOME OR EXPENSE ---");
        try {
            String typeInput = readNonEmptyString("Enter Type (INCOME / EXPENSE): ").toUpperCase();
            TransactionType type = TransactionType.valueOf(typeInput);
            String category = readNonEmptyString("Enter Category (e.g., Stipend, Food, Books, Transport): ");
            String description = readNonEmptyString("Enter Short Description: ");
            double amount = readDouble("Enter Amount (Rs.): ");

            FinanceEntry entry = lifeOS.recordFinance(type, category, description, amount);
            System.out.printf("[SUCCESS] Logged %s of Rs. %.2f (%s).%n", type, entry.getAmount(), entry.getCategory());
        } catch (IllegalArgumentException e) {
            System.out.println("[VALIDATION ERROR] Transaction type must be INCOME or EXPENSE.");
        } catch (BudgetLimitWarningException e) {
            System.out.println("[BUDGET WARNING] " + e.getMessage());
        } catch (InvalidLifeDataException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void handleUpdateMonthlyBudget() {
        try {
            System.out.printf("Current Monthly Expense Budget: Rs. %,.2f%n", lifeOS.getMonthlyExpenseBudget());
            double newLimit = readDouble("Enter new Monthly Expense Budget (Rs.): ");
            lifeOS.setMonthlyExpenseBudget(newLimit);
            System.out.printf("[SUCCESS] Monthly expense budget updated to Rs. %,.2f%n", newLimit);
        } catch (InvalidLifeDataException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // ------------------- MODULE 8: WELLNESS & MOOD JOURNAL -------------------

    private static void handleWellnessJournal() {
        printWellnessLogs();
        System.out.println("\n1. Log Today's Wellness & Reflection    0. Back");
        try {
            int sub = readInt("Choose action: ");
            if (sub == 1) {
                int mood = readInt("Rate today's mood (1 = Low to 5 = Excellent): ");
                double sleep = readDouble("Hours of sleep last night (e.g., 7.5): ");
                int water = readInt("Glasses of water consumed today: ");
                String note = readNonEmptyString("One win or gratitude highlight from today: ");

                lifeOS.addWellnessLog(mood, sleep, water, note);
                System.out.println("[SUCCESS] Wellness journal entry saved!");
            }
        } catch (InvalidLifeDataException e) {
            System.out.println("[VALIDATION ERROR] " + e.getMessage());
        }
    }

    private static void printWellnessLogs() {
        List<WellnessLog> logs = lifeOS.getWellnessLogs();
        System.out.println("DAILY WELLNESS, SLEEP & GRATITUDE JOURNAL:");
        System.out.println("+------------+-----------+-----------+--------------+----------------------------------+");
        System.out.println("| DATE       | MOOD      | SLEEP     | HYDRATION    | DAILY HIGHLIGHT / GRATITUDE      |");
        System.out.println("+------------+-----------+-----------+--------------+----------------------------------+");
        for (WellnessLog log : logs) {
            System.out.println(log);
        }
        System.out.println("+------------+-----------+-----------+--------------+----------------------------------+");
    }

    // =========================================================================
    // 5. INPUT HELPER METHODS
    // =========================================================================

    private static String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("[!] Input cannot be blank. Please try again.");
        }
    }

    private static int readInt(String prompt) {
        System.out.print(prompt);
        return Integer.parseInt(scanner.nextLine().trim());
    }

    private static double readDouble(String prompt) {
        System.out.print(prompt);
        return Double.parseDouble(scanner.nextLine().trim());
    }
}

