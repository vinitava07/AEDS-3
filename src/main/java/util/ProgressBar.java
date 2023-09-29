package util;

public class ProgressBar {
    private final String processName;
    private double hundredPerCent;
    private Timer timer;
    private boolean staticProcess;

    public ProgressBar(String processName, double hundredPerCent) {
        this.processName = processName;
        staticProcess = hundredPerCent < 0;
        this.hundredPerCent = hundredPerCent;
        this.timer = new Timer();
    }

    public void startProcess() {
        if(staticProcess) System.out.print("\nWorking on: " + this.processName);
        else System.out.println("\nWorking on: " + this.processName);
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
        builder.append(String.format("%.2f%% -- time: %.2f", xPerCent , this.timer.getCurrentTime()));
        System.out.print(builder);
        System.out.print("\r");
        System.out.flush();
    }

    public void setHundredPerCent(double hundredPerCent) {
        this.hundredPerCent = hundredPerCent;
    }

    public void done() {
        this.timer.stop();
        System.out.print("[####################################################################################################]100% " + this.processName + " done!! ==> ");
        printTime();
    }

    public void printTime() {
        if(staticProcess) {
            this.timer.stop();
            System.out.printf(" ==> done! took: %.3fs\n" , this.timer.getTime());
        } else System.out.printf("took: %.3fs\n\n" , this.timer.getTime());
    }

    public void printErrorMessage() {
        System.out.println("\nAn error occurred and could not finish " + this.processName);
    }
    public void printErrorMessage(String error) {
        System.out.println("\nThe error: \"" + error + "\" occurred.");
    }
}
