package Compiled;

import Configuration.*;
import Version2.MultiplyWithThreadPool;
import Version2.MultiplyWithThreads;
import Version3.MatrixMultiplier;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class CompiledVersion {

    final static Timer timer = new Timer();
    final static Memory memory = new Memory();

    final static int cores = Runtime.getRuntime().availableProcessors();
    public static void main(String[] args) {
        System.out.println("Deadline Dominator Concurrent and Parallelism");
        System.out.println("========================================");
        System.out.println("System Information:");
        System.out.println("  Processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("  Max Memory: " + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + " MB ");
        System.out.println("  Java Version: " + System.getProperty("java.version"));
        System.out.println("  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));

        // Create output directory for results
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String resultsDir = "Compiled Version results -" + timestamp;
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
                        String[] methodSelection = selectComprehensiveBenchmarkMethod(scanner);
                        runComprehensiveBenchmark(resultsDir, methodSelection[0], methodSelection[1]);
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

    private static void printMenu() {
        System.out.println("\nMethods Test Options:");
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

    private static String[] selectComprehensiveBenchmarkMethod(Scanner scanner) {
        System.out.println("\nChoose Method Type:");
        System.out.println("1. Sequential");
        System.out.println("2. Concurrent");
        System.out.println("3. Parallel");
        System.out.print("Enter your choice: ");
        String method = scanner.nextLine().trim();

        String concurrentType = "";

        switch (method) {
            case "1":
                return new String[]{"Sequential", ""};
            case "2":
                System.out.println("\nChoose Concurrent Method:");
                System.out.println("1. Multiple Threads (No ThreadPool)");
                System.out.println("2. ThreadPool - Assign Row Per Thread");
                System.out.println("3. ThreadPool - Assign Chunk Per Thread");
                System.out.print("Enter your choice: ");
                concurrentType = scanner.nextLine().trim();
                switch (concurrentType) {
                    case "1": return new String[]{"Concurrent", "MultipleThreads"};
                    case "2": return new String[]{"Concurrent", "RowPerThread"};
                    case "3": return new String[]{"Concurrent", "ChunkPerThread"};
                    default:
                        System.out.println("Invalid choice. Defaulting to Multiple Threads.");
                        return new String[]{"Concurrent", "MultipleThreads"};
                }
            case "3":
                return new String[]{"Parallel", ""};
            default:
                System.out.println("Invalid choice. Defaulting to Sequential.");
                return new String[]{"Sequential", ""};
        }
    }

    private static void runComprehensiveBenchmark(String resultsDir, String methodType, String subMethod) {
        int[] sizes = {500, 1000, 5000};
        int cores = Runtime.getRuntime().availableProcessors();

        try (PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/comprehensive-benchmark.csv"))) {
            writer.println("Size,Method,Time(ms),Memory(MB),CPUUtilization(%),Speedup,Efficiency(%)");

            for (int size : sizes) {
                System.out.println("Testing matrix size: " + size + "x" + size);

                // Generate matrices once per size
                Matrix A = new Matrix(size, size);
                Matrix B = new Matrix(size, size);
                A.assignRandom();
                B.assignRandom();

                // Run sequential always first to get baseline time & memory
                System.gc();
                memory.start();
                timer.start();
                A.multiplication(B);
                long sequentialTime = timer.end();
                long sequentialMemory = memory.end();

                System.out.printf("Sequential execution time for size %d: %d ms\n", size, sequentialTime);
                System.out.printf("Sequential memory usage for size %d: %d MB\n", size, sequentialMemory);

                if (methodType.equalsIgnoreCase("Sequential")) {
                    // Write sequential results only
                    writer.printf("%d,%s,%d,%d,%.2f,%.2f,%.2f\n",
                            size, "Sequential", sequentialTime, sequentialMemory, 100.0, 1.0, 100.0);
                }
                else if (methodType.equalsIgnoreCase("Concurrent")) {
                    long totalTime = 0;
                    long totalMemory = 0;
                    double speedup;
                    double efficiency;
                    double cpuUtilization;

                    switch (subMethod) {
                        case "MultipleThreads":
                            System.gc();
                            memory.start();
                            timer.start();
                            MultiplyWithThreads.multiplyWithThreads(A, B);
                            totalTime = timer.end();
                            totalMemory = memory.end();

                            System.out.printf("Multiple Threads execution time for size %d: %d ms\n", size, totalTime);
                            System.out.printf("Multiple Threads memory usage for size %d: %d MB\n", size, totalMemory);

                            speedup = (double) sequentialTime / totalTime;
                            efficiency = (speedup / cores) * 100;
                            cpuUtilization = efficiency;

                            writer.printf("%d,%s,%d,%d,%.2f,%.2f,%.2f\n",
                                    size, "Concurrent - Multiple Threads", totalTime, totalMemory, cpuUtilization, speedup, efficiency);
                            break;

                        case "RowPerThread":
                            System.gc();
                            memory.start();
                            timer.start();
                            MultiplyWithThreadPool.assignPerRow(A, B);
                            totalTime = timer.end();
                            totalMemory = memory.end();

                            System.out.printf("ThreadPool Row per Thread execution time for size %d: %d ms\n", size, totalTime);
                            System.out.printf("ThreadPool Row per Thread memory usage for size %d: %d MB\n", size, totalMemory);

                            speedup = (double) sequentialTime / totalTime;
                            efficiency = (speedup / cores) * 100;
                            cpuUtilization = efficiency;

                            writer.printf("%d,%s,%d,%d,%.2f,%.2f,%.2f\n",
                                    size, "Concurrent - ThreadPool Row Per Thread", totalTime, totalMemory, cpuUtilization, speedup, efficiency);
                            break;

                        case "ChunkPerThread":
                            System.gc();
                            memory.start();
                            timer.start();
                            MultiplyWithThreadPool.assignPerChunk(A, B);
                            totalTime = timer.end();
                            totalMemory = memory.end();

                            System.out.printf("ThreadPool Chunk per Thread execution time for size %d: %d ms\n", size, totalTime);
                            System.out.printf("ThreadPool Chunk per Thread memory usage for size %d: %d MB\n", size, totalMemory);

                            speedup = (double) sequentialTime / totalTime;
                            efficiency = (speedup / cores) * 100;
                            cpuUtilization = efficiency;

                            writer.printf("%d,%s,%d,%d,%.2f,%.2f,%.2f\n",
                                    size, "Concurrent - ThreadPool Chunk Per Thread", totalTime, totalMemory, cpuUtilization, speedup, efficiency);
                            break;

                        default:
                            System.out.println("Unknown concurrent method chosen.");
                            break;
                    }
                }
                else if (methodType.equalsIgnoreCase("Parallel")) {
                    // For Parallel method, use MatrixMultiplier (fork/join)
                    double[][] matA = MatrixMultiplier.generateRandomMatrix(size, size);
                    double[][] matB = MatrixMultiplier.generateRandomMatrix(size, size);

                    int threads = cores;
                    int iterations = 3;
                    double totalTime = 0;
                    long totalMemory = 0;

                    for (int i = 0; i < iterations; i++) {
                        System.gc();

                        Runtime runtime = Runtime.getRuntime();
                        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

                        long startTime = System.nanoTime();
                        MatrixMultiplier.multiplyMatrices(matA, matB, threads);
                        long endTime = System.nanoTime();

                        double execTime = (endTime - startTime) / 1_000_000.0;
                        totalTime += execTime;

                        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
                        totalMemory += (memoryAfter - memoryBefore) / (1024 * 1024);

                        System.out.printf("Parallel iteration %d execution time: %.2f ms\n", i + 1, execTime);
                    }

                    double avgTime = totalTime / iterations;
                    double avgMemory = (double) totalMemory / iterations;
                    double speedup = sequentialTime / avgTime;
                    double efficiency = (speedup / threads) * 100;
                    double cpuUtilization = efficiency;

                    writer.printf("%d,%s,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                            size, "Parallel - ForkJoinPool", avgTime, avgMemory, cpuUtilization, speedup, efficiency);

                    System.out.printf("Parallel average execution time for size %d: %.2f ms\n", size, avgTime);
                    System.out.printf("Parallel average memory usage for size %d: %.2f MB\n", size, avgMemory);
                }
            }

            System.out.println("Benchmark results saved to " + resultsDir + "/comprehensive-benchmark.csv");

        } catch (IOException | InterruptedException e) {
            System.err.println("Error during benchmark: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void runAllTests(String resultsDir) {
    }

    private static void runCacheEfficiencyTest(String resultsDir) {

    }

    private static void runCustomTest(Scanner scanner, String resultsDir) {

    }

    private static void runScalabilityTest(String resultsDir) {

    }

    private static void runThresholdOptimizationTest(String resultsDir) {

    }

    private static void runMemoryAnalysis(String resultsDir) {

    }

    private static void runQuickTest() {

    }
}