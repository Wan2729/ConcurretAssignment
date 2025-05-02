package Configuration;

public class Timer {
    long elapsed;
    long start;

    public Timer(){
        elapsed = 0;
    }

    public void start(){
        start = System.currentTimeMillis();
    }

    public long end(){
        elapsed = System.currentTimeMillis() - start;
        return elapsed;
    }

    public long getElapsed() {
        return elapsed;
    }
}
