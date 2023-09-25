package util;

public class ProgressBar {
    private final float hundredPerCent;

    public ProgressBar (float hundredPerCent) {
        this.hundredPerCent = hundredPerCent;
    }

    public void updateStatus(float doneAmount) {
        float xPerCent = ((doneAmount / this.hundredPerCent) * 100);
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        builder.append(']');
        for (int i = 0; i < 100; i++) {
            if(i <= xPerCent) builder.insert(i+1 , '#');
            else builder.insert(i+1 , ' ');
        }
        if(doneAmount == hundredPerCent) builder.append("100%");
        else builder.append(String.format("%.2f%%" , xPerCent));
        System.out.println(builder);
    }
    public void done() {
        System.out.println("[####################################################################################################]100% done!!");
    }
}
