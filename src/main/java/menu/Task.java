package menu;

public class Task {
    private static volatile String taskName;
    private static volatile int taskLength;
    public static volatile int progress;

    public static void init() {
        taskName = "";
        taskLength = 0;
    }
    public static void createTask(String taskName , int taskLength) {
        Task.taskName = taskName;
        Task.taskLength = taskLength;
    }

    public static int getTaskLength() {
        return Task.taskLength;
    }

    public static String getTaskName() {
        return Task.taskName;
    }

    public static void updateProgress(int i) {
        Task.progress = i;
    }

    public static int getProgress() {
        return progress;
    }

    public static void endTask() {
        taskName = "";
        taskLength = 0;
        progress = 0;
    }
}
