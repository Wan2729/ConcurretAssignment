package Version3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;

/**
 * Comprehensive test runner that integrates all matrix multiplication
 * implementations and provides detailed performance analysis
 */
public class MatrixTestRunner {

    /**
     * Main entry point for matrix multiplication testing
     */
    public static void main(String[] args) {
        System.out.println("Matrix Multiplication Performance Analysis");
        System.out.println("========================================");
        System.out.println("System Information:");
        System.out.println("  Processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("  Max Memory: " + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + " MB");
        System.out.println("  Java Version: " + System.getProperty("java.version"));
        System.out.println("  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));

        // Create output directory for results
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String resultsDir = "matrix-results-" + timestamp;
        new File(resultsDir).mkdir();

        try {
            Scanner scanner = new Scanner(System.in);
            int choice = 0;

            while (choice != 9) {
                printMenu();
                try {
                    choice = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    choice = 0;
                }

                switch (choice) {
                    case 1:
                        runQuickTest();
                        break;
                    case 2:
                        runComprehensiveBenchmark(resultsDir);
                        break;
                    case 3:
                        runMemoryAnalysis(resultsDir);
                        break;
                    case 4:
                        runThresholdOptimizationTest(resultsDir);
                        break;
                    case 5:
                        runScalabilityTest(resultsDir);
                        break;
                    case 6:
                        runCustomTest(scanner, resultsDir);
                        break;
                    case 7:
                        runCacheEfficiencyTest(resultsDir);
                        break;
                    case 8:
                        runAllTests(resultsDir);
                        break;
                    case 9:
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice, please try again");
                        break;
                }
            }

            scanner.close();

        } catch (Exception e) {
            System.err.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Print menu options
     */
    private static void printMenu() {
        System.out.println("\nMatrix Multiplication Test Options:");
        System.out.println("1. Quick Test (500x500 matrix)");
        System.out.println("2. Comprehensive Benchmark (various sizes)");
        System.out.println("3. Memory Analysis");
        System.out.println("4. Threshold Optimization Test");
        System.out.println("5. Scalability Test (threads vs. speedup)");
        System.out.println("6. Custom Test");
        System.out.println("7. Cache Efficiency Test");
        System.out.println("8. Run All Tests");
        System.out.println("9. Exit");
        System.out.print("Enter your choice: ");
    }

    /**
     * Run a quick test with a 500x500 matrix
     */
    private static void runQuickTest() {
        System.out.println("\nRunning quick test with 500x500 matrix...");

        int size = 500;
        int threads = Runtime.getRuntime().availableProcessors();

        System.out.println("Generating matrices...");
        double[][] A = MatrixMultiplier.generateRandomMatrix(size, size);
        double[][] B = MatrixMultiplier.generateRandomMatrix(size, size);

        System.out.println("Multiplying matrices using " + threads + " threads...");
        long startTime = System.nanoTime();
        double[][] C = MatrixMultiplier.multiplyMatrices(A, B, threads);
        long endTime = System.nanoTime();

        System.out.println("Multiplication completed in "
                + (endTime - startTime) / 1_000_000.0 + " ms");

        // Print a small sample of the result
        System.out.println("Sample of result matrix (top-left 3x3):");
        for (int i = 0; i < Math.min(3, C.length); i++) {
            for (int j = 0; j < Math.min(3, C[0].length); j++) {
                System.out.printf("%.2f\t", C[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * Run comprehensive benchmark with various matrix sizes
     */
    private static void runComprehensiveBenchmark(String resultsDir) {
        System.out.println("\nRunning comprehensive benchmark...");

        int[] sizes = {500, 1000, 2000, 3000};
        int[] threadCounts = {1, 2, 4, 8, Runtime.getRuntime().availableProcessors()};
        int iterations = 3;

        try ( PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/comprehensive-benchmark.csv"))) {
            // Write CSV header
            writer.println("Size,Threads,Time(ms),Memory(MB),CPUUtilization(%),Efficiency(%),WorkSteals");

            for (int size : sizes) {
                System.out.println("Testing matrix size: " + size + "x" + size);

                // Generate matrices once per size
                double[][] A = MatrixMultiplier.generateRandomMatrix(size, size);
                double[][] B = MatrixMultiplier.generateRandomMatrix(size, size);

                // Reference time for single thread
                double singleThreadTime = 0;

                for (int threads : threadCounts) {
                    System.out.println("  Using " + threads + " threads...");

                    // Run multiple iterations and average
                    double totalTime = 0;
                    long totalMemory = 0;
                    long totalSteals = 0;

                    for (int i = 0; i < iterations; i++) {
                        System.out.print("    Iteration " + (i + 1) + "... ");

                        // Force GC
                        System.gc();

                        // Get initial memory
                        Runtime runtime = Runtime.getRuntime();
                        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

                        // Execute multiplication
                        long startTime = System.nanoTime();
                        MatrixMultiplier.multiplyMatrices(A, B, threads);
                        long endTime = System.nanoTime();

                        // Calculate time
                        double executionTime = (endTime - startTime) / 1_000_000.0;
                        totalTime += executionTime;

                        // Calculate memory
                        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
                        totalMemory += (memoryAfter - memoryBefore) / (1024 * 1024);

                        System.out.println(executionTime + " ms");
                    }

                    // Calculate averages
                    double avgTime = totalTime / iterations;
                    double avgMemory = (double) totalMemory / iterations;
                    double avgSteals = (double) totalSteals / iterations;

                    // Record single thread time for efficiency calculation
                    if (threads == 1) {
                        singleThreadTime = avgTime;
                    }

                    // Calculate efficiency
                    double speedup = singleThreadTime / avgTime;
                    double efficiency = (speedup / threads) * 100;

                    // Calculate CPU utilization (estimated)
                    double cpuUtilization = (speedup / threads) * 100;

                    // Write to CSV
                    writer.printf("%d,%d,%.2f,%.2f,%.2f,%.2f,%.0f\n",
                            size, threads, avgTime, avgMemory, cpuUtilization, efficiency, avgSteals);

                    System.out.printf("    Average: %.2f ms, Memory: %.2f MB, Efficiency: %.2f%%\n",
                            avgTime, avgMemory, efficiency);
                }
            }

            System.out.println("Benchmark results saved to " + resultsDir + "/comprehensive-benchmark.csv");

        } catch (IOException e) {
            System.err.println("Error writing benchmark results: " + e.getMessage());
        }
    }

    /**
     * Run memory analysis
     */
    private static void runMemoryAnalysis(String resultsDir) {
        System.out.println("\nRunning memory analysis...");

        // Use the full memory profiler
        MatrixMemoryProfiler.runMemoryComparisonTests();

        // Run detailed profiling on a medium-sized matrix
        MatrixMemoryProfiler.profileMatrixMultiplication(1500,
                Runtime.getRuntime().availableProcessors());

        System.out.println("Memory analysis complete");
    }

    /**
     * Run threshold optimization test
     */
    private static void runThresholdOptimizationTest(String resultsDir) {
        System.out.println("\nRunning threshold optimization test...");

        int size = 2000;
        int threads = Runtime.getRuntime().availableProcessors();
        int[] thresholds = {32, 64, 128, 256, 512, 1024};
        int iterations = 3;

        try ( PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/threshold-optimization.csv"))) {
            // Write CSV header
            writer.println("Threshold,Time(ms)");

            // Generate matrices
            System.out.println("Generating " + size + "x" + size + " matrices...");
            double[][] A = MatrixMultiplier.generateRandomMatrix(size, size);
            double[][] B = MatrixMultiplier.generateRandomMatrix(size, size);
            double[][] transposedB = MatrixMultiplier.transpose(B);

            for (int threshold : thresholds) {
                System.out.println("Testing threshold: " + threshold);

                double totalTime = 0;

                for (int i = 0; i < iterations; i++) {
                    System.out.print("  Iteration " + (i + 1) + "... ");

                    // Force GC
                    System.gc();

                    // Create result matrix
                    double[][] C = new double[A.length][B[0].length];

                    // Create custom task with specified threshold
                    long startTime = System.nanoTime();

                    ForkJoinPool pool = new ForkJoinPool(threads);
                    pool.invoke(new SimpleMatrixBenchmark.CustomThresholdTask(
                            A, transposedB, C, 0, A.length, threshold, 32, true));
                    pool.shutdown();

                    long endTime = System.nanoTime();
                    double executionTime = (endTime - startTime) / 1_000_000.0;
                    totalTime += executionTime;

                    System.out.println(executionTime + " ms");
                }

                double avgTime = totalTime / iterations;
                writer.printf("%d,%.2f\n", threshold, avgTime);
                System.out.println("  Average execution time: " + avgTime + " ms");
            }

            System.out.println("Threshold optimization results saved to "
                    + resultsDir + "/threshold-optimization.csv");

        } catch (IOException e) {
            System.err.println("Error writing threshold results: " + e.getMessage());
        }
    }

    /**
     * Run scalability test
     */
    private static void runScalabilityTest(String resultsDir) {
        System.out.println("\nRunning scalability test...");

        int size = 2000;
        int maxThreads = Runtime.getRuntime().availableProcessors() * 2;
        int iterations = 3;

        try ( PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/scalability-test.csv"))) {
            // Write CSV header
            writer.println("Threads,Time(ms),Speedup,Efficiency(%)");

            // Generate matrices
            System.out.println("Generating " + size + "x" + size + " matrices...");
            double[][] A = MatrixMultiplier.generateRandomMatrix(size, size);
            double[][] B = MatrixMultiplier.generateRandomMatrix(size, size);

            // Baseline (single-threaded)
            System.out.println("Testing single-threaded performance (baseline)...");
            double baselineTime = 0;

            for (int i = 0; i < iterations; i++) {
                System.out.print("  Iteration " + (i + 1) + "... ");

                // Force GC
                System.gc();

                long startTime = System.nanoTime();
                MatrixMultiplier.multiplyMatrices(A, B, 1);
                long endTime = System.nanoTime();

                double executionTime = (endTime - startTime) / 1_000_000.0;
                baselineTime += executionTime;

                System.out.println(executionTime + " ms");
            }

            baselineTime /= iterations;
            System.out.println("Baseline (1 thread): " + baselineTime + " ms");
            writer.printf("%d,%.2f,%.2f,%.2f\n", 1, baselineTime, 1.0, 100.0);

            // Test with increasing thread counts
            for (int threads = 2; threads <= maxThreads; threads++) {
                System.out.println("Testing with " + threads + " threads...");

                double totalTime = 0;

                for (int i = 0; i < iterations; i++) {
                    System.out.print("  Iteration " + (i + 1) + "... ");

                    // Force GC
                    System.gc();

                    long startTime = System.nanoTime();
                    MatrixMultiplier.multiplyMatrices(A, B, threads);
                    long endTime = System.nanoTime();

                    double executionTime = (endTime - startTime) / 1_000_000.0;
                    totalTime += executionTime;

                    System.out.println(executionTime + " ms");
                }

                double avgTime = totalTime / iterations;
                double speedup = baselineTime / avgTime;
                double efficiency = (speedup / threads) * 100;

                writer.printf("%d,%.2f,%.2f,%.2f\n", threads, avgTime, speedup, efficiency);
                System.out.printf("  Average: %.2f ms, Speedup: %.2fx, Efficiency: %.2f%%\n",
                        avgTime, speedup, efficiency);
            }

            System.out.println("Scalability test results saved to "
                    + resultsDir + "/scalability-test.csv");

        } catch (IOException e) {
            System.err.println("Error writing scalability results: " + e.getMessage());
        }
    }

    /**
     * Run custom test based on user input
     */
    private static void runCustomTest(Scanner scanner, String resultsDir) {
        System.out.println("\nCustom Test Configuration");

        // Get matrix size
        System.out.print("Enter matrix size (e.g., 1000): ");
        int size = 1000;
        try {
            size = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, using default size: " + size);
        }

        // Get number of threads
        System.out.print("Enter number of threads (or 0 for all available): ");
        int threads = 0;
        try {
            threads = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, using all available processors");
        }

        if (threads <= 0) {
            threads = Runtime.getRuntime().availableProcessors();
        }

        // Get number of iterations
        System.out.print("Enter number of iterations: ");
        int iterations = 3;
        try {
            iterations = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, using default iterations: " + iterations);
        }

        // Run the test
        System.out.println("\nRunning custom test with:");
        System.out.println("  Matrix size: " + size + "x" + size);
        System.out.println("  Threads: " + threads);
        System.out.println("  Iterations: " + iterations);

        try ( PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/custom-test.csv"))) {
            // Write CSV header
            writer.println("Iteration,Time(ms),Memory(MB)");

            // Generate matrices
            System.out.println("Generating matrices...");
            double[][] A = MatrixMultiplier.generateRandomMatrix(size, size);
            double[][] B = MatrixMultiplier.generateRandomMatrix(size, size);

            double totalTime = 0;
            long totalMemory = 0;

            for (int i = 0; i < iterations; i++) {
                System.out.print("Iteration " + (i + 1) + "... ");

                // Force GC
                System.gc();

                // Get initial memory
                Runtime runtime = Runtime.getRuntime();
                long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

                // Execute multiplication
                long startTime = System.nanoTime();
                MatrixMultiplier.multiplyMatrices(A, B, threads);
                long endTime = System.nanoTime();

                // Calculate time
                double executionTime = (endTime - startTime) / 1_000_000.0;
                totalTime += executionTime;

                // Calculate memory
                long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
                long memory = (memoryAfter - memoryBefore) / (1024 * 1024);
                totalMemory += memory;

                writer.printf("%d,%.2f,%d\n", i + 1, executionTime, memory);
                System.out.println(executionTime + " ms, Memory: " + memory + " MB");
            }

            // Calculate averages
            double avgTime = totalTime / iterations;
            double avgMemory = (double) totalMemory / iterations;

            writer.println("\nAverage,Time(ms),Memory(MB)");
            writer.printf("Average,%.2f,%.2f\n", avgTime, avgMemory);

            System.out.println("\nAverage execution time: " + avgTime + " ms");
            System.out.println("Average memory usage: " + avgMemory + " MB");
            System.out.println("Custom test results saved to " + resultsDir + "/custom-test.csv");

        } catch (IOException e) {
            System.err.println("Error writing custom test results: " + e.getMessage());
        }
    }

    /**
     * Run cache efficiency test
     */
    private static void runCacheEfficiencyTest(String resultsDir) {
        System.out.println("\nRunning cache efficiency test...");

        int size = 2000;
        int threads = Runtime.getRuntime().availableProcessors();
        int[] blockSizes = {8, 16, 32, 64, 128, 256};
        int iterations = 3;

        try ( PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/cache-efficiency.csv"))) {
            // Write CSV header
            writer.println("BlockSize,StandardTime(ms),TransposedTime(ms),Improvement(%)");

            // Generate matrices
            System.out.println("Generating " + size + "x" + size + " matrices...");
            double[][] A = MatrixMultiplier.generateRandomMatrix(size, size);
            double[][] B = MatrixMultiplier.generateRandomMatrix(size, size);
            double[][] transposedB = MatrixMultiplier.transpose(B);

            for (int blockSize : blockSizes) {
                System.out.println("Testing block size: " + blockSize);

                // Test standard multiplication
                System.out.println("  Standard multiplication (without transpose):");
                double standardTotalTime = 0;

                for (int i = 0; i < iterations; i++) {
                    System.out.print("    Iteration " + (i + 1) + "... ");

                    double[][] C = new double[A.length][B[0].length];

                    long startTime = System.nanoTime();

                    ForkJoinPool pool = new ForkJoinPool(threads);
                    pool.invoke(new SimpleMatrixBenchmark.CustomThresholdTask(
                            A, B, C, 0, A.length, 128, blockSize, false));
                    pool.shutdown();

                    long endTime = System.nanoTime();
                    double executionTime = (endTime - startTime) / 1_000_000.0;
                    standardTotalTime += executionTime;

                    System.out.println(executionTime + " ms");
                }

                double standardAvgTime = standardTotalTime / iterations;
                System.out.println("  Standard average: " + standardAvgTime + " ms");

                // Test transposed multiplication
                System.out.println("  Optimized multiplication (with transpose):");
                double transposedTotalTime = 0;

                for (int i = 0; i < iterations; i++) {
                    System.out.print("    Iteration " + (i + 1) + "... ");

                    double[][] C = new double[A.length][B[0].length];

                    long startTime = System.nanoTime();

                    ForkJoinPool pool = new ForkJoinPool(threads);
                    pool.invoke(new SimpleMatrixBenchmark.CustomThresholdTask(
                            A, transposedB, C, 0, A.length, 128, blockSize, true));
                    pool.shutdown();

                    long endTime = System.nanoTime();
                    double executionTime = (endTime - startTime) / 1_000_000.0;
                    transposedTotalTime += executionTime;

                    System.out.println(executionTime + " ms");
                }

                double transposedAvgTime = transposedTotalTime / iterations;
                System.out.println("  Transposed average: " + transposedAvgTime + " ms");

                // Calculate improvement
                double improvement = ((standardAvgTime - transposedAvgTime) / standardAvgTime) * 100;
                System.out.printf("  Improvement: %.2f%%\n", improvement);

                writer.printf("%d,%.2f,%.2f,%.2f\n",
                        blockSize, standardAvgTime, transposedAvgTime, improvement);
            }

            System.out.println("Cache efficiency results saved to "
                    + resultsDir + "/cache-efficiency.csv");

        } catch (IOException e) {
            System.err.println("Error writing cache efficiency results: " + e.getMessage());
        }
    }

    /**
     * Run all tests
     */
    private static void runAllTests(String resultsDir) {
        System.out.println("\nRunning all tests (this may take a while)...");

        // Run all tests in sequence
        runQuickTest();
        runComprehensiveBenchmark(resultsDir);
        runMemoryAnalysis(resultsDir);
        runThresholdOptimizationTest(resultsDir);
        runScalabilityTest(resultsDir);
        runCacheEfficiencyTest(resultsDir);

        System.out.println("\nAll tests completed. Results saved to " + resultsDir);
    }
}
