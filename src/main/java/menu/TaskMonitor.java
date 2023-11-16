package menu;

import java.util.concurrent.atomic.AtomicLong;

public class TaskMonitor {
    private static String taskName;
    private static AtomicLong progress;
    private static long hundredPercent;

    public static void init() {
        TaskMonitor.progress = new AtomicLong(0);
        TaskMonitor.hundredPercent = 0;
    }

    public static void buildTask(String taskName) {
        TaskMonitor.taskName = taskName;
        TaskMonitor.hundredPercent = 0;
        TaskMonitor.progress.set(0);
    }

    public static void newTask(String taskName, long hundredPercent) {
        TaskMonitor.taskName = taskName;
        TaskMonitor.hundredPercent = hundredPercent;
        TaskMonitor.progress.set(0);
    }

    public static String getTaskName() {
        return taskName;
    }

    public static void setHundredPercent(long hundredPercent) {
        TaskMonitor.hundredPercent = hundredPercent;
    }

    public static long getHundredPercent() {
        return TaskMonitor.hundredPercent;
    }

    public static void updateProgress(long progress) {
        TaskMonitor.progress.set(progress);
    }

    public static int getProgress() {
        return (int) ((((double)TaskMonitor.progress.get()) / TaskMonitor.hundredPercent) * 100);
    }

    public static void end() {
        TaskMonitor.progress.set(TaskMonitor.hundredPercent);
    }
}