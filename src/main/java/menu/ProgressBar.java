package menu;

import javax.swing.*;

public class ProgressBar {
    private static String processName;
    private static JProgressBar progressBar;
    private static long hundredPerCent;
    private static long progressValue;

    public static void init(String processName , long hundredPerCent) {
        ProgressBar.hundredPerCent = hundredPerCent;
        ProgressBar.processName = processName;
        ProgressBar.progressBar = new JProgressBar(0 , 10000);
        ProgressBar.progressBar.setBorderPainted(true);
    }

    public static void setProcessName(String processName) {
        ProgressBar.processName = processName;
    }

    public static void setHundredPerCent(long hundredPerCent) {
        ProgressBar.progressBar.setValue(0);
        ProgressBar.hundredPerCent = hundredPerCent;
    }

    public static void updateProgress(long progressValue) {
        double per10Thousand = (((double) progressValue) / ProgressBar.hundredPerCent) * 10000;
        int value = (int) Math.ceil(per10Thousand);
        progressBar.setValue(value);
        if(value >= progressBar.getMaximum()) {
            JOptionPane.showMessageDialog(null, (processName + " done!!"));
            progressBar.setValue(0);
        }
    }

    public static JProgressBar getProgressBar() {
        return ProgressBar.progressBar;
    }
}
