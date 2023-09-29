package util;

public class Timer {
    private long start;
    private long end;
    public Timer(){}
    public void start(){start = System.currentTimeMillis();}
    public void stop(){end = System.currentTimeMillis();}
    public float getTime(){return ((float) (end - start) / 1000);}
}
