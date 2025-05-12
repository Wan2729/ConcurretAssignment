package Version3;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.sun.management.OperatingSystemMXBean;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Performance analyzer for matrix multiplication
 * Provides detailed metrics on CPU, memory, and thread utilization
 */
public class MatrixMultiplierPerformanceAnalyzer {
    
    // Flag to enable/disable detailed monitoring
    private boolean detailedMonitoring = false;
    
    // Monitoring thread
    private Thread monitoringThread;
    
    // Storage for metrics
    private Map<String, Double> metrics = new HashMap<>();
    
    /**
     * Enable detailed performance monitoring
     * This starts a background thread that samples performance metrics
     */
    public void startMonitoring() {
        if (monitoringThread != null && monitoringThread.isAlive()) {
            return; // Already monitoring
        }
        
        detailedMonitoring = true;
        
        // Reset metrics
        metrics.clear();
        metrics.put("peakMemory", 0.0);
        metrics.put("avgCpuLoad", 0.0);
        metrics.put("maxCpuLoad", 0.0);
        metrics.put("samplesCount", 0.0);
        
        // Start monitoring thread
        monitoringThread = new Thread(() -> {
            try {
                OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                    OperatingSystemMXBean.class);
                
                while (detailedMonitoring) {
                    // Sample CPU load
                    double cpuLoad = osBean.getProcessCpuLoad() * 100;
                    
                    // Update metrics
                    metrics.put("avgCpuLoad", 
                        (metrics.get("avgCpuLoad") * metrics.get("samplesCount") + cpuLoad) / 
                        (metrics.get("samplesCount") + 1));
                    
                    metrics.put("maxCpuLoad", Math.max(metrics.get("maxCpuLoad"), cpuLoad));
                    metrics.put("samplesCount", metrics.get("samplesCount") + 1);
                    
                    // Sample memory usage
                    Runtime runtime = Runtime.getRuntime();
                    double memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / 
                                      (1024.0 * 1024.0);
                    metrics.put("peakMemory", Math.max(metrics.get("peakMemory"), memoryUsed));
                    
                    // Sleep for sampling interval
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                // Thread was interrupted
            }
        });
        
        monitoringThread.setDaemon(true);
        monitoringThread.start();
    }
    
    /**
     * Stop performance monitoring
     */
    public void stopMonitoring() {
        detailedMonitoring = false;
        if (monitoringThread != null) {
            monitoringThread.interrupt();
            try {
                monitoringThread.join(1000);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }
    
    /**
     * Get the collected metrics
     * @return Map of metric name to value
     */
    public Map<String, Double> getMetrics() {
        return new HashMap<>(metrics);
    }
    
    /**
     * Run a comprehensive performance analysis
     * Tests different matrix sizes and thread counts, monitoring and comparing
     * runtime performance metrics
     */
    public void runPerformanceAnalysis(String outputFile) {
        int[] sizes = {500, 1000, 2000, 3000};
        int[] threadCounts = {1, 2, 4, 8, Runtime.getRuntime().availableProcessors()};
        
        System.out.println("Starting Matrix Multiplication Performance Analysis");
        System.out.println("==================================================");
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Write CSV header
            writer.println("Size,Threads,ExecutionTime(ms),Memory(MB),CPUUtilization(%),ThreadEfficiency(%),TasksCreated,WorkSteals");
            
            // For each matrix size and thread count
            for (int size : sizes) {
                System.out.println("\nAnalyzing matrices of size " + size + "×" + size);
                
                // Generate test matrices once per size
                double[][] A = MatrixMultiplier.generateRandomMatrix(size, size);
                double[][] B = MatrixMultiplier.generateRandomMatrix(size, size);
                
                // Reference time for single-threaded execution
                double singleThreadTime = 0;
                
                for (int threads : threadCounts) {
                    System.out.println("  Testing with " + threads + " threads");
                    
                    // Start monitoring
                    startMonitoring();
                    
                    // Warm-up run
                    runTest(A, B, threads);
                    
                    // Reset metrics for actual measurement
                    stopMonitoring();
                    startMonitoring();
                    
                    // Measure execution time
                    long startTime = System.nanoTime();
                    
                    // Use custom pool with desired thread count
                    ForkJoinPool pool = new ForkJoinPool(threads);
                    double[][] C = new double[A.length][B[0].length];
                    
                    // Use transpose optimization
                    double[][] transposedB = MatrixMultiplier.transpose(B);
                    pool.invoke(new MatrixMultiplyTask(A, transposedB, C, 0, A.length, true));
                    
                    long stealCount = pool.getStealCount();
                    pool.shutdown();
                    
                    // Calculate execution time
                    long endTime = System.nanoTime();
                    double executionTime = (endTime - startTime) / 1_000_000.0; // ms
                    
                    // Record single-thread time for comparison
                    if (threads == 1) {
                        singleThreadTime = executionTime;
                    }
                    
                    // Stop monitoring
                    stopMonitoring();
                    
                    // Calculate metrics
                    double speedup = singleThreadTime / executionTime;
                    double efficiency = (speedup / threads) * 100; // percentage
                    
                    // Get metrics from monitor
                    Map<String, Double> performanceMetrics = getMetrics();
                    double memory = performanceMetrics.get("peakMemory");
                    double cpuUtilization = performanceMetrics.get("avgCpuLoad");
                    
                    // Print results
                    System.out.printf("    Time: %.2f ms, Speedup: %.2fx, Efficiency: %.2f%%, " +
                                     "Memory: %.2f MB, CPU: %.2f%%\n",
                                     executionTime, speedup, efficiency, memory, cpuUtilization);
                    
                    // Write to CSV
                    writer.printf("%d,%d,%.2f,%.2f,%.2f,%.2f,%d,%d\n",
                                 size, threads, executionTime, memory, cpuUtilization,
                                 efficiency, 0, stealCount);
                }
            }
            
            System.out.println("\nPerformance analysis complete. Results saved to " + outputFile);
            
        } catch (IOException e) {
            System.err.println("Error writing performance data: " + e.getMessage());
        }
    }
    
    /**
     * Run a single test with specified parameters
     */
    private void runTest(double[][] A, double[][] B, int threads) {
        ForkJoinPool pool = new ForkJoinPool(threads);
        double[][] C = new double[A.length][B[0].length];
        double[][] transposedB = MatrixMultiplier.transpose(B);
        pool.invoke(new MatrixMultiplyTask(A, transposedB, C, 0, A.length, true));
        pool.shutdown();
    }
    
    /**
     * Analyze GC behavior during matrix multiplication
     * This helps identify memory pressure and garbage collection overhead
     */
    public void analyzeGCBehavior(int matrixSize, int threads) {
        System.out.println("Analyzing Garbage Collection Behavior");
        System.out.println("====================================");
        
        try {
            // Register for GC notifications
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName gcName = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
            
            // Generate test matrices
            System.out.println("Generating matrices of size " + matrixSize + "×" + matrixSize);
            double[][] A = MatrixMultiplier.generateRandomMatrix(matrixSize, matrixSize);
            double[][] B = MatrixMultiplier.generateRandomMatrix(matrixSize, matrixSize);
            
            // Force GC before test
            System.gc();
            Thread.sleep(1000);
            
            System.out.println("Starting multiplication with " + threads + " threads");
            
            // Start time
            long startTime = System.nanoTime();
            
            // Perform multiplication
            ForkJoinPool pool = new ForkJoinPool(threads);
            double[][] C = new double[A.length][B[0].length];
            double[][] transposedB = MatrixMultiplier.transpose(B);
            pool.invoke(new MatrixMultiplyTask(A, transposedB, C, 0, A.length, true));
            pool.shutdown();
            
            // End time
            long endTime = System.nanoTime();
            double executionTime = (endTime - startTime) / 1_000_000.0;
            
            // Get memory usage
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            System.out.println("Execution completed in " + executionTime + " ms");
            System.out.println("Memory usage: " + (usedMemory / (1024*1024)) + " MB");
            System.out.println("Total memory: " + (totalMemory / (1024*1024)) + " MB");
            System.out.println("Free memory: " + (freeMemory / (1024*1024)) + " MB");
            
        } catch (Exception e) {
            System.err.println("Error analyzing GC behavior: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generate a thread profile report to better understand thread utilization
     */
    public void generateThreadProfile(int matrixSize, int threads) {
        System.out.println("Generating Thread Profile");
        System.out.println("========================");
        
        try {
            // Generate test matrices
            double[][] A = MatrixMultiplier.generateRandomMatrix(matrixSize, matrixSize);
            double[][] B = MatrixMultiplier.generateRandomMatrix(matrixSize, matrixSize);
            
            // Create pool with monitoring
            ForkJoinPool pool = new ForkJoinPool(threads);
            
            // Thread for monitoring pool stats
            Thread monitorThread = new Thread(() -> {
                try {
                    System.out.println("Time(ms),ActiveThreads,QueuedTasks,StealCount,ParallelismLevel");
                    long startTime = System.currentTimeMillis();
                    
                    while (!Thread.currentThread().isInterrupted()) {
                        long elapsed = System.currentTimeMillis() - startTime;
                        int activeThreads = pool.getActiveThreadCount();
                        long queuedTasks = pool.getQueuedTaskCount();
                        long stealCount = pool.getStealCount();
                        double parallelism = pool.getParallelism();
                        
                        System.out.printf("%d,%d,%d,%d,%.2f\n", 
                            elapsed, activeThreads, queuedTasks, stealCount, parallelism);
                        
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    // Expected when operation is complete
                }
            });
            
            monitorThread.setDaemon(true);
            monitorThread.start();
            
            // Execute multiplication
            System.out.println("Starting multiplication with " + threads + " threads");
            double[][] C = new double[A.length][B[0].length];
            double[][] transposedB = MatrixMultiplier.transpose(B);
            pool.invoke(new MatrixMultiplyTask(A, transposedB, C, 0, A.length, true));
            
            // Stop monitoring thread
            monitorThread.interrupt();
            try {
                monitorThread.join(1000);
            } catch (InterruptedException e) {
                // Ignore
            }
            
            // Show final stats
            System.out.println("\nFinal Thread Statistics:");
            System.out.println("  Peak active threads: " + pool.getActiveThreadCount());
            System.out.println("  Total steal count: " + pool.getStealCount());
            System.out.println("  Pool size: " + pool.getPoolSize());
            
            pool.shutdown();
            
        } catch (Exception e) {
            System.err.println("Error generating thread profile: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        MatrixMultiplierPerformanceAnalyzer analyzer = new MatrixMultiplierPerformanceAnalyzer();
        
        // Run complete performance analysis
        analyzer.runPerformanceAnalysis("matrix_performance.csv");
        
        // Analyze GC behavior for large matrix
        analyzer.analyzeGCBehavior(2000, Runtime.getRuntime().availableProcessors());
        
        // Generate thread profile
        analyzer.generateThreadProfile(1000, Runtime.getRuntime().availableProcessors());
    }
}