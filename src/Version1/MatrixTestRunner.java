package Version1;

import Configuration.Matrix;
import Configuration.Memory;
import Configuration.Timer;
import Version3.MatrixMultiplier;
import Version3.SimpleMatrixBenchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;

public class MatrixTestRunner {
    final static Timer timer = new Timer();
    final static Memory memory = new Memory();

    public static void main(String[] args) {
        System.out.println("Matrix Sequential Multiplication Performance Analysis");
        System.out.println("========================================");
        System.out.println("System Information:");
        System.out.println("  Processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("  Max Memory: " + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + " MB");
        System.out.println("  Java Version: " + System.getProperty("java.version"));
        System.out.println("  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));

        // Create output directory for results
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String resultsDir = "Version1-matrix-results-" + timestamp;
        boolean created = new File(resultsDir).mkdir();
        System.out.println("Directory creation result: " + created);

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
                        runThresholdOptimizationTestSequential(resultsDir);
                        break;
                    case 5:
                        runScalabilityTestSequential(resultsDir);
                        break;
                    case 6:
                        runCacheEfficiencyTestSequential(resultsDir);
                        break;
                    case 7:
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
        System.exit(0);
    }

    private static void printMenu() {
        System.out.println("\nMatrix Multiplication Test Options:");
        System.out.println("1. Quick Test (500x500 matrix)");
        System.out.println("2. Comprehensive Benchmark (various sizes)");
        System.out.println("3. Memory Analysis");
        System.out.println("4. Threshold Optimization Test");
        System.out.println("5. Scalability Test");
        System.out.println("6. Cache Efficiency Test");
        System.out.println("9. Exit");
        System.out.print("Enter your choice: ");
    }

    public static void runQuickTest(){
        Matrix A = new Matrix(500, 500);
        Matrix B = new Matrix(500, 500);
        A.assignRandom();
        B.assignRandom();

        // Perform multiplication & track progress
        timer.start();
        Matrix result = A.multiplication(B);
        long elapsedTime = timer.end();

        System.out.println(500 + "x" + 500 + " Time taken with Sequential: " + elapsedTime + " ms");
    }

    private static void runComprehensiveBenchmark(String resultsDir) {
        int[] sizes = {500, 1000, 5000};

        try ( PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/comprehensive-benchmark.csv"))) {
            // Write CSV header
            writer.println("Size,Threads,Time(ms),Memory(MB),CPUUtilization(%),Speedup, Efficiency(%)");

            for (int size : sizes) {
                long totalTime = 0;
                long totalMemory = 0;

                System.out.println("Testing matrix size: " + size + "x" + size);

                // Generate matrices once per size
                Matrix A = new Matrix(size, size);
                Matrix B = new Matrix(size, size);
                A.assignRandom();
                B.assignRandom();

                /*
                Using Sequential Processing
                 */
                System.gc();
                memory.start();
                timer.start();
                A.multiplication(B);
                totalTime = timer.end();
                totalMemory = memory.end();

                System.out.printf("Execution time for sequential size %d: %d ms\n", size, totalTime);
                System.out.printf("Memory Utilization for sequential size %d: %d MB\n", size, totalMemory);

                // Write to CSV
                writer.printf("%d,%s,%d,%d,%.2f,%.2f,%.2f\n",
                        size, "Sequential", totalTime, totalMemory, 100.0, 1.0, 100.0);
                /*
                Finish using sequential processing
                 */
            }

            System.out.println("Benchmark results saved to " + resultsDir + "/comprehensive-benchmark.csv");

        } catch (IOException e) {
            System.err.println("Error writing benchmark results: " + e.getMessage());
        }
    }

    private static void runMemoryAnalysis(String resultsDir) {
        System.out.println("\nRunning memory analysis...");

        // Use the full memory profiler
        MatrixMemoryProfiler.runMemoryComparisonTestsSequential();

        // Run detailed profiling on a medium-sized matrix
        MatrixMemoryProfiler.profileMatrixMultiplicationSequential(1500);

        System.out.println("Memory analysis complete");
    }

    private static void runThresholdOptimizationTestSequential(String resultsDir) {
        System.out.println("\nRunning threshold optimization test (Sequential Execution)...");

        int size = 2000;
        int[] thresholds = {32, 64, 128, 256, 512, 1024}; // These may have limited impact in sequential mode
        int iterations = 3;

        try (PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/threshold-optimization-sequential.csv"))) {
            // Write CSV header
            writer.println("Threshold,Time(ms)");

            Timer timer = new Timer();
            // Generate matrices
            System.out.println("Generating " + size + "x" + size + " matrices...");
            Matrix A = new Matrix(size, size);
            A.assignRandom();
            Matrix B = new Matrix(size, size);
            B.assignRandom();
            Matrix transposedB = B.transpose();

            for (int threshold : thresholds) {
                System.out.println("Testing threshold: " + threshold);

                double totalTime = 0;

                for (int i = 0; i < iterations; i++) {
                    System.out.print("  Iteration " + (i + 1) + "... ");

                    // Force GC
                    System.gc();

                    // Create result matrix
                    Matrix C = new Matrix(size, size);

                    // Execute sequential multiplication
                    timer.start();
                    C = A.multiplication(transposedB);
                    long elapsed = timer.end();

                    totalTime += elapsed;

                    System.out.println(elapsed + " ms");
                }

                double avgTime = totalTime / iterations;
                writer.printf("%d,%.2f\n", threshold, avgTime);
                System.out.println("  Average execution time: " + avgTime + " ms");
            }

            System.out.println("Threshold optimization results saved to "
                    + resultsDir + "/threshold-optimization-sequential.csv");

        } catch (IOException e) {
            System.err.println("Error writing threshold results: " + e.getMessage());
        }
    }

    private static void runScalabilityTestSequential(String resultsDir) {
        System.out.println("\nRunning scalability test (Sequential Execution)...");

        int[] sizes = {500, 1000, 2000, 3000}; // Scalability test based on matrix size
        int iterations = 3;

        try (PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/scalability-test-sequential.csv"))) {
            // Write CSV header
            writer.println("Size,Time(ms)");

            for (int size : sizes) {
                System.out.println("Testing matrix size: " + size);

                double totalTime = 0;

                for (int i = 0; i < iterations; i++) {
                    System.out.print("  Iteration " + (i + 1) + "... ");

                    // Force GC (optional, remove if unnecessary)
                    System.gc();

                    // Generate matrices
                    Timer timer = new Timer();
                    Matrix A = new Matrix(size, size);
                    A.assignRandom();
                    Matrix B = new Matrix(size, size);
                    B.assignRandom();
                    Matrix C = new Matrix(size, size);

                    // Execute sequential matrix multiplication
                    timer.start();
                    C = A.multiplication(B);
                    long elapsed = timer.end();

                    totalTime += elapsed;

                    System.out.println(elapsed + " ms");
                }

                double avgTime = totalTime / iterations;
                writer.printf("%d,%.2f\n", size, avgTime);
                System.out.printf("  Average: %.2f ms\n", avgTime);
            }

            System.out.println("Scalability test results saved to " + resultsDir + "/scalability-test-sequential.csv");

        } catch (IOException e) {
            System.err.println("Error writing scalability results: " + e.getMessage());
        }
    }

    private static void runCacheEfficiencyTestSequential(String resultsDir) {
        System.out.println("\nRunning cache efficiency test (Sequential Execution)...");

        int size = 2000;
        int[] blockSizes = {8, 16, 32, 64, 128, 256}; // Evaluating different block sizes
        int iterations = 3;

        try (PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/cache-efficiency-sequential.csv"))) {
            // Write CSV header
            writer.println("BlockSize,StandardTime(ms),TransposedTime(ms),Improvement(%)");

            // Generate matrices
            System.out.println("Generating " + size + "x" + size + " matrices...");
            Timer timer = new Timer();
            Matrix A = new Matrix(size, size);
            A.assignRandom();
            Matrix B = new Matrix(size, size);
            B.assignRandom();
            Matrix transposedB = B.transpose();

            for (int blockSize : blockSizes) {
                System.out.println("Testing block size: " + blockSize);

                // Test standard multiplication
                System.out.println("  Standard multiplication (without transpose):");
                double standardTotalTime = 0;

                for (int i = 0; i < iterations; i++) {
                    System.out.print("    Iteration " + (i + 1) + "... ");

                    Matrix C = new Matrix(size, size);
                    timer.start();

                    C = A.multiplicationBlocked(B,blockSize); // Sequential multiplication
                    long elapsed = timer.end();
                    standardTotalTime += elapsed;

                    System.out.println(elapsed + " ms");
                }

                double standardAvgTime = standardTotalTime / iterations;
                System.out.println("  Standard average: " + standardAvgTime + " ms");

                // Test transposed multiplication
                System.out.println("  Optimized multiplication (with transpose):");
                double transposedTotalTime = 0;

                for (int i = 0; i < iterations; i++) {
                    System.out.print("    Iteration " + (i + 1) + "... ");

                    Matrix C = new Matrix(size, size);
                    timer.start();

                    C = A.multiplicationBlocked(transposedB,blockSize); // Sequential multiplication with transpose
                    long elapsed = timer.end();
                    transposedTotalTime += elapsed;

                    System.out.println(elapsed + " ms");
                }

                double transposedAvgTime = transposedTotalTime / iterations;
                System.out.println("  Transposed average: " + transposedAvgTime + " ms");

                // Calculate improvement
                double improvement = ((standardAvgTime - transposedAvgTime) / standardAvgTime) * 100;
                System.out.printf("  Improvement: %.2f%%\n", improvement);

                writer.printf("%d,%.2f,%.2f,%.2f\n", blockSize, standardAvgTime, transposedAvgTime, improvement);
            }

            System.out.println("Cache efficiency results saved to " + resultsDir + "/cache-efficiency-sequential.csv");

        } catch (IOException e) {
            System.err.println("Error writing cache efficiency results: " + e.getMessage());
        }
    }

    private static void runAllTests(String resultsDir) {
        System.out.println("\nRunning all tests (this may take a while)...");

        // Run all tests in sequence
        runQuickTest();
        runComprehensiveBenchmark(resultsDir);
        runMemoryAnalysis(resultsDir);
        runThresholdOptimizationTestSequential(resultsDir);
        runScalabilityTestSequential(resultsDir);
        runCacheEfficiencyTestSequential(resultsDir);

        System.out.println("\nAll tests completed. Results saved to " + resultsDir);
    }
}
