package Version1;

import Configuration.Matrix;
import Configuration.Timer;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * Utility for detailed memory profiling of matrix multiplication operations
 */
public class MatrixMemoryProfiler {

    private static long lastUsedMemory = 0;
    private static long maxUsedMemory = 0;
    private static long gcCount = 0;
    private static long gcTime = 0;

    /**
     * Reset all memory statistics
     */
    public static void resetMemoryStats() {
        lastUsedMemory = 0;
        maxUsedMemory = 0;
        gcCount = 0;
        gcTime = 0;

        // Force garbage collection
        System.gc();
        System.gc();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Ignore
        }
    }

    /**
     * Get current memory usage in MB
     */
    public static double getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        lastUsedMemory = memory;
        maxUsedMemory = Math.max(maxUsedMemory, memory);
        return memory / (1024.0 * 1024.0);
    }

    /**
     * Get max memory usage recorded since reset
     */
    public static double getMaxMemoryUsage() {
        return maxUsedMemory / (1024.0 * 1024.0);
    }

    /**
     * Get memory delta since last check
     */
    public static double getMemoryDelta() {
        Runtime runtime = Runtime.getRuntime();
        long currentMemory = runtime.totalMemory() - runtime.freeMemory();
        long delta = currentMemory - lastUsedMemory;
        lastUsedMemory = currentMemory;
        maxUsedMemory = Math.max(maxUsedMemory, currentMemory);
        return delta / (1024.0 * 1024.0);
    }

    /**
     * Update GC statistics
     */
    private static void updateGCStats() {
        long totalGCCount = 0;
        long totalGCTime = 0;

        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            long count = gcBean.getCollectionCount();
            if (count >= 0) {
                totalGCCount += count;
            }

            long time = gcBean.getCollectionTime();
            if (time >= 0) {
                totalGCTime += time;
            }
        }

        gcCount = totalGCCount;
        gcTime = totalGCTime;
    }

    /**
     * Get GC statistics
     */
    public static String getGCStats() {
        updateGCStats();
        return String.format("GC Count: %d, GC Time: %d ms", gcCount, gcTime);
    }

    /**
     * Print detailed memory report for heap and non-heap memory
     */
    public static void printMemoryReport() {
        System.out.println("\nDETAILED MEMORY REPORT");
        System.out.println("=====================");

        // General memory info
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = allocatedMemory - freeMemory;

        System.out.println("Runtime Memory:");
        System.out.println("  Max memory: " + (maxMemory / (1024 * 1024)) + " MB");
        System.out.println("  Allocated memory: " + (allocatedMemory / (1024 * 1024)) + " MB");
        System.out.println("  Free memory: " + (freeMemory / (1024 * 1024)) + " MB");
        System.out.println("  Used memory: " + (usedMemory / (1024 * 1024)) + " MB");
        System.out.println("  Max used memory: " + (maxUsedMemory / (1024 * 1024)) + " MB");

        // Heap memory
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

        System.out.println("\nHeap Memory:");
        System.out.println("  Initial: " + (heapUsage.getInit() / (1024 * 1024)) + " MB");
        System.out.println("  Used: " + (heapUsage.getUsed() / (1024 * 1024)) + " MB");
        System.out.println("  Committed: " + (heapUsage.getCommitted() / (1024 * 1024)) + " MB");
        System.out.println("  Max: " + (heapUsage.getMax() / (1024 * 1024)) + " MB");

        System.out.println("\nNon-Heap Memory:");
        System.out.println("  Initial: " + (nonHeapUsage.getInit() / (1024 * 1024)) + " MB");
        System.out.println("  Used: " + (nonHeapUsage.getUsed() / (1024 * 1024)) + " MB");
        System.out.println("  Committed: " + (nonHeapUsage.getCommitted() / (1024 * 1024)) + " MB");
        System.out.println("  Max: " + (nonHeapUsage.getMax() / (1024 * 1024)) + " MB");

        // Memory pools
        System.out.println("\nMemory Pools:");
        List<MemoryPoolMXBean> memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean memoryPoolBean : memoryPoolBeans) {
            String name = memoryPoolBean.getName();
            MemoryUsage usage = memoryPoolBean.getUsage();

            System.out.println("  " + name + ":");
            System.out.println("    Used: " + (usage.getUsed() / (1024 * 1024)) + " MB");
            System.out.println("    Committed: " + (usage.getCommitted() / (1024 * 1024)) + " MB");
            System.out.println("    Max: " + (usage.getMax() / (1024 * 1024)) + " MB");
        }

        // GC information
        System.out.println("\nGarbage Collection:");
        updateGCStats();
        System.out.println("  Total collections: " + gcCount);
        System.out.println("  Total time spent: " + gcTime + " ms");

        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            System.out.println("  " + gcBean.getName() + ":");
            System.out.println("    Collection count: " + gcBean.getCollectionCount());
            System.out.println("    Collection time: " + gcBean.getCollectionTime() + " ms");
        }
    }

    /**
     * Run a matrix multiplication with memory profiling
     */
    public static void profileMatrixMultiplicationSequential(int size) {
        System.out.println("MEMORY PROFILING: Matrix Size " + size + "x" + size + ", Sequential Execution");
        System.out.println("====================================================================");
        Timer timer = new Timer();

        // Reset memory stats
        resetMemoryStats();

        // Initial memory state
        System.out.println("Initial memory usage: " + String.format("%.2f MB", getCurrentMemoryUsage()));

        // Generate matrices
        System.out.println("Generating matrices...");
        Matrix A = new Matrix(size, size);
        A.assignRandom();
        Matrix B = new Matrix(size, size);
        B.assignRandom();
        System.out.println("Memory after matrix generation: " + String.format("%.2f MB", getCurrentMemoryUsage()));
        System.out.println("Memory change: " + String.format("%.2f MB", getMemoryDelta()));

        // Transpose B for optimization
        System.out.println("Transposing matrix B...");
        Matrix transposedB = B.transpose();
        System.out.println("Memory after transpose: " + String.format("%.2f MB", getCurrentMemoryUsage()));
        System.out.println("Memory change: " + String.format("%.2f MB", getMemoryDelta()));

        // Create result matrix
        System.out.println("Creating result matrix...");
        Matrix C = new Matrix(size, size);
        System.out.println("Memory after result matrix creation: " + String.format("%.2f MB", getCurrentMemoryUsage()));
        System.out.println("Memory change: " + String.format("%.2f MB", getMemoryDelta()));

        // Execute multiplication in sequential mode
        System.out.println("Executing matrix multiplication sequentially...");
        timer.start();
        C = A.multiplication(transposedB); // Sequential matrix multiplication
        long elapsed = timer.end();
        System.out.println("Memory after multiplication: " + String.format("%.2f MB", getCurrentMemoryUsage()));
        System.out.println("Memory change: " + String.format("%.2f MB", getMemoryDelta()));
        System.out.println("Execution time: " + elapsed + " ms");

        // GC stats
        System.out.println("GC stats: " + getGCStats());

        // Print detailed memory report
        printMemoryReport();

        System.out.println("\nMax memory usage during entire operation: "
                + String.format("%.2f MB", getMaxMemoryUsage()));
    }

    /**
     * Run a comparison of different matrix sizes with memory profiling
     */
    public static void runMemoryComparisonTestsSequential() {
        int[] sizes = {500, 1000, 2000, 3000};

        System.out.println("MEMORY COMPARISON TESTS (Sequential Execution)");
        System.out.println("==============================================");

        System.out.println("Size\tMemory(MB)\tTime(ms)\tGC Count\tGC Time(ms)");

        for (int size : sizes) {
            // Reset memory stats
            resetMemoryStats();

            Timer timer = new Timer();
            // Generate matrices
            Matrix A = new Matrix(size, size);
            A.assignRandom();
            Matrix B = new Matrix(size, size);
            B.assignRandom();
            Matrix transposedB = B.transpose();
            Matrix C = new Matrix(size, size);

            // Force GC
            System.gc();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Ignore
            }

            // Execute multiplication sequentially
            System.out.println("Executing matrix multiplication sequentially for size " + size + "...");
            timer.start();
            C = A.multiplication(transposedB); // Uses sequential multiplication
            long elapsed = timer.end();

            // Calculate metrics
            double maxMemory = getMaxMemoryUsage();
            updateGCStats();

            // Print results
            System.out.printf("%d\t%.2f\t\t%d\t\t%d\t\t%d\n",
                    size, maxMemory, elapsed, gcCount, gcTime);
        }
    }

    public static void main(String[] args) {
        // Example usage
        System.out.println("Memory Profiling Utility for Matrix Multiplication");

        // Profile a specific matrix size
        profileMatrixMultiplicationSequential(1000);

        // Run comparison tests
        runMemoryComparisonTestsSequential();
    }
}
