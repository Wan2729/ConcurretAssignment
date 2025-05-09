package version3.parallel.Forkjoin;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ThreadMXBean;

public class MatrixMultiplier {

    // Configurable parameters
    private static final int[] MATRIX_SIZES = {500, 1000, 2000, 5000};
    private static final int NUM_WARMUP_RUNS = 2;
    private static final int NUM_BENCHMARK_RUNS = 5;
    private static final String RESULTS_FILE = "matrix_multiplication_results.csv";

    // Memory tracking
    private static MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    public static double[][] generateRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        Random rand = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = rand.nextDouble() * 100;
            }
        }
        return matrix;
    }

    // Helper method to print memory usage
    private static void printMemoryUsage(String label) {
        MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryBean.getNonHeapMemoryUsage();

        System.out.println(label + " Memory Usage:");
        System.out.println("  Heap Used: " + formatMemory(heapMemoryUsage.getUsed()));
        System.out.println("  Heap Committed: " + formatMemory(heapMemoryUsage.getCommitted()));
        System.out.println("  Non-Heap Used: " + formatMemory(nonHeapMemoryUsage.getUsed()));
    }

    private static String formatMemory(long bytes) {
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    // Method to register MBean for profiling
    private static void registerProfilingMBean() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("version3.parallel.Forkjoin:type=MatrixMultiplier");
            // Register custom MBean if needed
            System.out.println("JMX profiling enabled. Connect with JVisualVM to this process.");
            System.out.println("Process ID: " + ManagementFactory.getRuntimeMXBean().getName());
        } catch (Exception e) {
            System.err.println("Failed to register profiling MBean: " + e.getMessage());
        }
    }

    // Helper method to write results to CSV file
    private static void writeResultsToFile(String matrixSize, String algorithm, double executionTime,
            long memoryUsed, int numThreads) {
        try ( BufferedWriter writer = new BufferedWriter(new FileWriter(RESULTS_FILE, true))) {
            if (!java.nio.file.Files.exists(java.nio.file.Paths.get(RESULTS_FILE))) {
                writer.write("Matrix Size,Algorithm,Execution Time (ms),Memory Used (MB),Threads\n");
            }
            writer.write(matrixSize + "," + algorithm + "," + executionTime + ","
                    + (memoryUsed / (1024.0 * 1024.0)) + "," + numThreads + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to results file: " + e.getMessage());
        }
    }

    // Improved benchmark method
    private static void runBenchmark(int size, int numThreads) {
        System.out.println("\n========================================");
        System.out.println("Running benchmark for " + size + "x" + size + " matrices with " + numThreads + " threads");
        System.out.println("========================================");

        // Generate matrices
        System.out.println("Generating matrices...");
        double[][] A = generateRandomMatrix(size, size);
        double[][] B = generateRandomMatrix(size, size);
        double[][] C = new double[size][size];

        // Warm-up runs
        System.out.println("Performing " + NUM_WARMUP_RUNS + " warm-up runs...");
        for (int i = 0; i < NUM_WARMUP_RUNS; i++) {
            C = new double[size][size];
            ForkJoinPool pool = new ForkJoinPool(numThreads);
            pool.invoke(new MatrixMultiplyTask(A, B, C, 0, size));
            pool.shutdown();
        }

        // Force garbage collection before benchmark
        System.gc();
        try {
            Thread.sleep(1000); // Give GC time to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Benchmark runs
        System.out.println("Performing " + NUM_BENCHMARK_RUNS + " benchmark runs...");
        double totalExecutionTime = 0;
        long totalMemoryUsed = 0;

        for (int run = 0; run < NUM_BENCHMARK_RUNS; run++) {
            C = new double[size][size];
            long memoryBefore = memoryBean.getHeapMemoryUsage().getUsed();
            long startTime = System.nanoTime();

            ForkJoinPool pool = new ForkJoinPool(numThreads);
            pool.invoke(new MatrixMultiplyTask(A, B, C, 0, size));
            pool.shutdown();

            try {
                // Wait for all tasks to complete
                pool.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            long endTime = System.nanoTime();
            long memoryAfter = memoryBean.getHeapMemoryUsage().getUsed();

            double executionTime = (endTime - startTime) / 1_000_000.0; // Convert to ms
            long memoryUsed = memoryAfter - memoryBefore;

            System.out.printf("Run %d: Execution Time: %.2f ms, Memory Used: %.2f MB%n",
                    run + 1, executionTime, memoryUsed / (1024.0 * 1024.0));

            totalExecutionTime += executionTime;
            totalMemoryUsed += memoryUsed;
        }

        // Calculate and print average results
        double avgExecutionTime = totalExecutionTime / NUM_BENCHMARK_RUNS;
        long avgMemoryUsed = totalMemoryUsed / NUM_BENCHMARK_RUNS;

        System.out.println("\nAverage Results:");
        System.out.printf("Execution Time: %.2f ms%n", avgExecutionTime);
        System.out.printf("Memory Used: %.2f MB%n", avgMemoryUsed / (1024.0 * 1024.0));

        // Write results to file
        writeResultsToFile(size + "x" + size, "ForkJoin", avgExecutionTime, avgMemoryUsed, numThreads);
    }

    public static void main(String[] args) {
        // Register for profiling
        registerProfilingMBean();

        // Print system information
        System.out.println("System Information:");
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Max memory: " + formatMemory(Runtime.getRuntime().maxMemory()));

        // Run benchmarks for different matrix sizes with different thread counts
        for (int size : MATRIX_SIZES) {
            // Run with default number of threads (equal to processor count)
            runBenchmark(size, Runtime.getRuntime().availableProcessors());

            // Optionally run with different thread counts
            // Uncomment if you want to test with specific thread counts
            // runBenchmark(size, 2);
            // runBenchmark(size, 4);
            // runBenchmark(size, 8);
        }

        System.out.println("\nAll benchmarks completed. Results saved to " + RESULTS_FILE);
    }
}
