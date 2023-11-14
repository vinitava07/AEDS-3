package util;
import java.util.concurrent.atomic.AtomicLong;

public class ProgressMonitor extends Thread {
    private Timer time;
    private boolean staticProcess;
    private AtomicLong status;
    private long hundredPerCent;

    public ProgressMonitor(String processName, AtomicLong paramater, long hundredPerCent) {
        this.setName(processName);
        this.time = new Timer();
        this.staticProcess = hundredPerCent < 0;
        this.status = paramater;
        this.hundredPerCent = hundredPerCent;
    }

    public ProgressMonitor(String processName) {
        this.setName(processName);
        this.staticProcess = true;
        this.time = new Timer();
        this.hundredPerCent = 1;
        this.status = new AtomicLong(0);
    }

    @Override
    public void run() {
        try{
            System.out.print("\033[?25l\nWorking on: " + this.getName() + " \033[s");

            Thread.sleep(1);
            time.start();

            if(staticProcess) {
                int c = 0;
                String[] dots = {".  " , ".. " , "..."};
                while(status.get() != hundredPerCent) {
                    System.out.printf("\033[u %s", dots[c++ % 3]);
                    // System.out.println("x: " + cursor.x + "  y: " + cursor.y);
                    Thread.sleep(300);
                }
            } else {
                System.out.print(" - \033[s");
                while(status.get() != hundredPerCent) {
                    System.out.print("\033[u" + ((status.get() * 100) / hundredPerCent) + "%");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void endProcess() throws Exception{
        status.set(hundredPerCent);
        time.stop();
        System.out.print("\033[u100%\n" + this.getName() + " Done!! took: " + time.getTime() + "s\033[?25h\n\n");
        while (this.isAlive()) ;
        this.interrupt();
    }
}
