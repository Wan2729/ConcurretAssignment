package Version2;

import Configuration.Matrix;
import Configuration.Memory;
import Configuration.Timer;
import Version3.MatrixMultiplier;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class MatrixTestRunner {
    final static Timer timer = new Timer();
    final static Memory memory = new Memory();
    final static int cores = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        System.out.println("Matrix Concurrent Multiplication Performance Analysis");
        System.out.println("========================================");
        System.out.println("System Information:");
        System.out.println("  Processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("  Max Memory: " + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + " MB");
        System.out.println("  Java Version: " + System.getProperty("java.version"));
        System.out.println("  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));

        // Create output directory for results
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String resultsDir = "Version2-matrix-results-" + timestamp;
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
        System.out.println("Lalu");
        System.exit(0);
    }

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
    private static void runQuickTest() throws InterruptedException {
        System.out.println("\nRunning quick test with 500x500 matrix...");

        int size = 500;
        int threads = Runtime.getRuntime().availableProcessors();

        System.out.println("Generating matrices...");
        Matrix A = new Matrix(size, size);
        Matrix B = new Matrix(size, size);
        Matrix result = new Matrix(size, size);

        A.assignRandom();
        B.assignRandom();

        System.out.println("Multiplying matrices using ThreadPoolPerChunkTask, " + threads + " threads...");
        long startTime = System.nanoTime();
        result = MultiplyWithThreadPool.assignPerChunk(A, B);
        long endTime = System.nanoTime();

        System.out.println("Multiplication completed in "
                + (endTime - startTime) / 1_000_000.0 + " ms");

        // Print a small sample of the result
        System.out.println("Sample of result matrix (top-left 3x3):");
        for (int i = 0; i < Math.min(3, result.matrix.length); i++) {
            for (int j = 0; j < Math.min(3, result.matrix[0].length); j++) {
                System.out.printf("%.2f\t", result.matrix[i][j]);
            }
            System.out.println();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Run comprehensive benchmark with various matrix sizes
     */
    private static void runComprehensiveBenchmark(String resultsDir) {
        int[] sizes = {500, 1000, 2000, 3000};

        try ( PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/comprehensive-benchmark.csv"))) {
            // Write CSV header
            writer.println("Size,Threads,Time(ms),Memory(MB),CPUUtilization(%),Speedup, Efficiency(%)");

            for (int size : sizes) {
                long singleThreadTime = 0;
                long totalTime = 0;
                long totalMemory = 0;
                double speedup;
                double efficiency;
                double cpuUtilization;

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
                singleThreadTime = timer.end();
                totalMemory = memory.end();

                System.out.printf("Execution time for sequential size %d: %d ms\n", size, singleThreadTime);
                System.out.printf("Memory Utilization for sequential size %d: %d MB\n", size, totalMemory);

                // Write to CSV
                writer.printf("%d,%s,%d,%d,%.2f,%.2f,%.2f\n",
                        size, "Sequential", singleThreadTime, totalMemory, 100.0, 1.0, 100.0);
                /*
                Finish using sequential processing
                 */

                /*
                Using Multiple Threads (No ThreadPool)
                 */
                System.gc();
                memory.start();
                timer.start();
                MultiplyWithThreads.multiplyWithThreads(A, B);
                totalTime = timer.end();
                totalMemory = memory.end();

                System.out.printf("Execution time for sequential size %d: %d ms\n", size, totalTime);
                System.out.printf("Memory Utilization for sequential size %d: %d MB\n", size, totalMemory);

                // Calculate speedup and efficiency
                speedup = (double) singleThreadTime / (double) totalTime;
                efficiency = (speedup / size) * 100; // have number of thread = sizes

                // Calculate CPU utilization (estimated)
                cpuUtilization = (speedup / size) * 100; // have number of thread = sizes

                // Write to CSV
                writer.printf("%d,%s,%d,%d,%.2f,%.2f,%.2f\n",
                        size, "Concurrent - Multiple Threads (No ThreadPool)", totalTime, totalMemory, cpuUtilization, speedup, efficiency);
                /*
                Finish using Multiple Threads (No ThreadPool)
                 */

                /*
                Using ThreadPool (Assign Thread per Row)
                 */
                System.gc();
                memory.start();
                timer.start();
                MultiplyWithThreadPool.assignPerRow(A, B);
                totalTime = timer.end();
                totalMemory = memory.end();

                System.out.printf("Execution time for sequential size %d: %d ms\n", size, totalTime);
                System.out.printf("Memory Utilization for sequential size %d: %d MB\n", size, totalMemory);

                // Calculate speedup and efficiency
                speedup = (double) singleThreadTime / (double) totalTime;
                efficiency = (speedup / cores) * 100; // have fixed number of thread

                // Calculate CPU utilization (estimated)
                cpuUtilization = (speedup / cores) * 100; // have fixed number of thread

                // Write to CSV
                writer.printf("%d,%s,%d,%d,%.2f,%.2f,%.2f\n",
                        size, "Concurrent - ThreadPool (Assign Thread per Row)", totalTime, totalMemory, cpuUtilization, speedup, efficiency);
                /*
                Finish ThreadPool (Assign Thread per Row)
                 */

                /*
                Using ThreadPool (Assign Thread per Chunk)
                 */
                System.gc();
                memory.start();
                timer.start();
                MultiplyWithThreadPool.assignPerChunk(A, B);
                totalTime = timer.end();
                totalMemory = memory.end();

                System.out.printf("Execution time for sequential size %d: %d ms\n", size, totalTime);
                System.out.printf("Memory Utilization for sequential size %d: %d MB\n", size, totalMemory);

                // Calculate speedup and efficiency
                speedup = (double) singleThreadTime / (double) totalTime;
                efficiency = (speedup / cores) * 100; // have fixed number of thread

                // Calculate CPU utilization (estimated)
                cpuUtilization = (speedup / cores) * 100; // have fixed number of thread

                // Write to CSV
                writer.printf("%d,%s,%d,%d,%.2f,%.2f,%.2f\n",
                        size, "Concurrent - ThreadPool (Assign Thread per Row)", totalTime, totalMemory, cpuUtilization, speedup, efficiency);
                /*
                Finish using ThreadPool (Assign Thread per Chunk)
                 */
            }

            System.out.println("Benchmark results saved to " + resultsDir + "/comprehensive-benchmark.csv");

        } catch (IOException e) {
            System.err.println("Error writing benchmark results: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Run memory analysis
     */
    private static void runMemoryAnalysis(String resultsDir) {
        System.out.println("\nRunning memory analysis...");
    }

    /**
     * Run threshold optimization test
     */
    private static void runThresholdOptimizationTest(String resultsDir) {
        System.out.println("\nRunning threshold optimization test...");
    }

    /**
     * Run scalability test
     */
    private static void runScalabilityTest(String resultsDir) {
        System.out.println("\nRunning scalability test...");
    }

    /**
     * Run custom test based on user input
     */
    private static void runCustomTest(Scanner scanner, String resultsDir) {
        System.out.println("\nCustom Test Configuration");
    }

    /**
     * Run cache efficiency test
     */
    private static void runCacheEfficiencyTest(String resultsDir) {
        System.out.println("\nRunning cache efficiency test...");
    }

    /**
     * Run all tests
     */
    private static void runAllTests(String resultsDir) {
        System.out.println("\nRunning all tests (this may take a while)...");

        // Run all tests in sequence
        try {
            runQuickTest();
            runComprehensiveBenchmark(resultsDir);
            runMemoryAnalysis(resultsDir);
            runThresholdOptimizationTest(resultsDir);
            runScalabilityTest(resultsDir);
            runCacheEfficiencyTest(resultsDir);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\nAll tests completed. Results saved to " + resultsDir);
    }
}
