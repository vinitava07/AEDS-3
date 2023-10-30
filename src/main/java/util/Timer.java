package util;

public class Timer {
    private long start;
    private long end;
    public Timer(){}
    public void start(){start = System.currentTimeMillis();}
    public void stop(){end = System.currentTimeMillis();}
    public double getTime(){return ((double) (end - start) / 1000);}
    public double getCurrentTime(){return ((double) (System.currentTimeMillis() - start) / 1000);}
}
