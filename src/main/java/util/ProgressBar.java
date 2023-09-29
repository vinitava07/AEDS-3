package util;

public class ProgressBar {
    private final String processName;
    private double hundredPerCent;
    private Timer timer;

    public ProgressBar(String processName, double hundredPerCent) {
        this.processName = processName;
        this.hundredPerCent = hundredPerCent;
        this.timer = new Timer();
    }

    public void startProcess() {
        System.out.println("\nWorking on: " + this.processName);
        this.timer.start();
    }

    public void updateStatus(double doneAmount) {
        double xPerCent = ((doneAmount / this.hundredPerCent) * 100);
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(']');
        for (int i = 0; i < 100; i++) {
            if (i <= xPerCent) builder.insert(i + 1, '#');
            else builder.insert(i + 1, ' ');
        }
        builder.append(String.format("%.2f%%", xPerCent));
        System.out.print(builder);
        System.out.print("\r");
        System.out.flush();
    }

    public void setHundredPerCent(double hundredPerCent) {
        this.hundredPerCent = hundredPerCent;
    }

    public void done() {
        this.timer.stop();
        System.out.print("[####################################################################################################]100% " + this.processName + " done!! -- took: ");
        System.out.printf("%.2f s\n\n" , this.timer.getTime());
    }

    public void printErrorMessage() {
        System.out.println("\nAn error occurred and could not finish " + this.processName);
    }
    public void printErrorMessage(String error) {
        System.out.println("\nThe error: \"" + error + "\" occurred.");
    }
}
