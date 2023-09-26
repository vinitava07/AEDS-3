package util;

public class ProgressBar {
    private final String processName;
    private final double hundredPerCent;

    public ProgressBar (String processName , double hundredPerCent) {
        this.processName = processName;
        this.hundredPerCent = hundredPerCent;
    }

    public void startProcess() {
        System.out.println("Working on: " + this.processName);
    }
    public void updateStatus(double doneAmount) {
        double xPerCent = ((doneAmount / this.hundredPerCent) * 100);
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(']');
        for (int i = 0; i < 100; i++) {
            if(i <= xPerCent) builder.insert(i+1 , '#');
            else builder.insert(i+1 , ' ');
        }
        boolean flag = (Math.floor(xPerCent) % 2 == 0);
        if(flag) builder.append(String.format("%.2f%% \\" , xPerCent));
        else builder.append(String.format("%.2f%% /" , xPerCent));
        System.out.print(builder);
        System.out.print("\r");
    }
    public void done() {
        System.out.println("[####################################################################################################]100% " + this.processName + " done!!");
    }
}
