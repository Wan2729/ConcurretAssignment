package Configuration;

public class Memory {
    private long memoryBefore;
    private long memoryAfter;
    private long totalMemory;
    private Runtime runtime = Runtime.getRuntime();

    public Memory(){
        totalMemory=0;
    }

    public void start(){
        memoryBefore = runtime.totalMemory() - runtime.freeMemory();
    }

    public long end(){
        memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        totalMemory += (memoryAfter - memoryBefore) / (1024 * 1024);
        return totalMemory;
    }

    public long getTotalMemory() {
        return totalMemory;
    }
}
