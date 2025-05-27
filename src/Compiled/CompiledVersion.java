package Compiled;

import Configuration.*;
import Version1.MatrixMemoryProfiler;
import Version2.MultiplyWithThreadPool;
import Version2.MultiplyWithThreads;
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
                String[] methodSelection = null;
                switch (choice) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        methodSelection = selectComprehensiveBenchmarkMethod(scanner);
                        break;
                }

                switch (choice) {
                    case 1:
                        runQuickTest(methodSelection[0], methodSelection[1]);
                        break;
                    case 2:
                        runComprehensiveBenchmark(resultsDir, methodSelection[0], methodSelection[1]);
                        break;
                    case 3:
                        runMemoryAnalysis(resultsDir, methodSelection[0], methodSelection[1]);
                        break;
                    case 4:
                        runThresholdOptimizationTest(resultsDir, methodSelection[0], methodSelection[1]);
                        break;
                    case 5:
                        runScalabilityTest(resultsDir, methodSelection[0], methodSelection[1]);
                        break;
                    case 6:
                        runCacheEfficiencyTest(resultsDir, methodSelection[0], methodSelection[1]);
                        break;
                    case 7:
                        runAllTests(resultsDir,"All","All");
                        break;
                    case 8:
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
        System.out.println("6. Cache Efficiency Test");
        System.out.println("7. Run All Tests");
        System.out.println("8. Exit");
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


    private static void runAllTests(String resultsDir, String methodType, String subMethod) {
        System.out.println("\nRunning all tests (this may take a while)...");

        String[] methodTypes = {"Sequential", "Concurrent", "Parallel"};
        String[] concurrentSubMethods = {"MultipleThreads", "RowPerThread", "ChunkPerThread"};

        try {
            if (methodType.equalsIgnoreCase("All")) {
                // Run all method types and their respective sub-methods
                for (String method : methodTypes) {
                    if (method.equals("Concurrent")) {
                        for (String sub : concurrentSubMethods) {
                            System.out.println("\n--- Running tests for " + method + " - " + sub + " ---");
                            runQuickTest(method, sub);
                            runComprehensiveBenchmark(resultsDir, method, sub);
                            runMemoryAnalysis(resultsDir, method, sub);
                            runThresholdOptimizationTest(resultsDir, method, sub);
                            runScalabilityTest(resultsDir, method, sub);
                            runCacheEfficiencyTest(resultsDir, method, sub);
                        }
                    } else {
                        System.out.println("\n--- Running tests for " + method + " ---");
                        runQuickTest(method, "");
                        runComprehensiveBenchmark(resultsDir, method, "");
                        runMemoryAnalysis(resultsDir, method, "");
                        runThresholdOptimizationTest(resultsDir, method, "");
                        runScalabilityTest(resultsDir, method, "");
                        runCacheEfficiencyTest(resultsDir, method, "");
                    }
                }
            } else {
                // Run selected method and sub-method only
                runQuickTest(methodType, subMethod);
                runComprehensiveBenchmark(resultsDir, methodType, subMethod);
                runMemoryAnalysis(resultsDir, methodType, subMethod);
                runThresholdOptimizationTest(resultsDir, methodType, subMethod);
                runScalabilityTest(resultsDir, methodType, subMethod);
                runCacheEfficiencyTest(resultsDir, methodType, subMethod);
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\nAll tests completed. Results saved to " + resultsDir);
    }




    private static void runCacheEfficiencyTest(String resultsDir, String methodType, String subMethod) {
        System.out.println("\nRunning cache efficiency test (" + methodType + ")...");

        int size = 2000;
        int[] blockSizes = {8, 16, 32, 64, 128, 256};
        int iterations = 3;

        boolean isParallel = methodType.equalsIgnoreCase("parallel");

        String fileName = isParallel ? "/cache-efficiency.csv" : "/cache-efficiency-sequential.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + fileName))) {
            writer.println("BlockSize,StandardTime(ms),TransposedTime(ms),Improvement(%)");

            System.out.println("Generating " + size + "x" + size + " matrices...");

            if (isParallel) {
                double[][] A = MatrixMultiplier.generateRandomMatrix(size, size);
                double[][] B = MatrixMultiplier.generateRandomMatrix(size, size);
                double[][] transposedB = MatrixMultiplier.transpose(B);
                int threads = Runtime.getRuntime().availableProcessors();

                for (int blockSize : blockSizes) {
                    System.out.println("Testing block size: " + blockSize);

                    double standardTotalTime = 0;
                    for (int i = 0; i < iterations; i++) {
                        System.out.print("    Iteration " + (i + 1) + "... ");
                        double[][] C = new double[A.length][B[0].length];

                        long start = System.nanoTime();
                        ForkJoinPool pool = new ForkJoinPool(threads);
                        pool.invoke(new SimpleMatrixBenchmark.CustomThresholdTask(
                                A, B, C, 0, A.length, 128, blockSize, false));
                        pool.shutdown();
                        long end = System.nanoTime();

                        double time = (end - start) / 1_000_000.0;
                        standardTotalTime += time;
                        System.out.println(time + " ms");
                    }

                    double standardAvg = standardTotalTime / iterations;

                    double transposedTotalTime = 0;
                    for (int i = 0; i < iterations; i++) {
                        System.out.print("    Iteration " + (i + 1) + "... ");
                        double[][] C = new double[A.length][B[0].length];

                        long start = System.nanoTime();
                        ForkJoinPool pool = new ForkJoinPool(threads);
                        pool.invoke(new SimpleMatrixBenchmark.CustomThresholdTask(
                                A, transposedB, C, 0, A.length, 128, blockSize, true));
                        pool.shutdown();
                        long end = System.nanoTime();

                        double time = (end - start) / 1_000_000.0;
                        transposedTotalTime += time;
                        System.out.println(time + " ms");
                    }

                    double transposedAvg = transposedTotalTime / iterations;
                    double improvement = ((standardAvg - transposedAvg) / standardAvg) * 100;

                    System.out.printf("  Standard Avg: %.2f ms, Transposed Avg: %.2f ms, Improvement: %.2f%%\n",
                            standardAvg, transposedAvg, improvement);
                    writer.printf("%d,%.2f,%.2f,%.2f\n", blockSize, standardAvg, transposedAvg, improvement);
                }

            } else { // Sequential path
                Timer timer = new Timer();
                Matrix A = new Matrix(size, size);
                A.assignRandom();
                Matrix B = new Matrix(size, size);
                B.assignRandom();
                Matrix transposedB = B.transpose();

                for (int blockSize : blockSizes) {
                    System.out.println("Testing block size: " + blockSize);

                    double standardTotalTime = 0;
                    for (int i = 0; i < iterations; i++) {
                        System.out.print("    Iteration " + (i + 1) + "... ");
                        Matrix C = new Matrix(size, size);
                        timer.start();
                        C = A.multiplicationBlocked(B, blockSize);
                        long elapsed = timer.end();
                        standardTotalTime += elapsed;
                        System.out.println(elapsed + " ms");
                    }

                    double standardAvg = standardTotalTime / iterations;

                    double transposedTotalTime = 0;
                    for (int i = 0; i < iterations; i++) {
                        System.out.print("    Iteration " + (i + 1) + "... ");
                        Matrix C = new Matrix(size, size);
                        timer.start();
                        C = A.multiplicationBlocked(transposedB, blockSize);
                        long elapsed = timer.end();
                        transposedTotalTime += elapsed;
                        System.out.println(elapsed + " ms");
                    }

                    double transposedAvg = transposedTotalTime / iterations;
                    double improvement = ((standardAvg - transposedAvg) / standardAvg) * 100;

                    System.out.printf("  Standard Avg: %.2f ms, Transposed Avg: %.2f ms, Improvement: %.2f%%\n",
                            standardAvg, transposedAvg, improvement);
                    writer.printf("%d,%.2f,%.2f,%.2f\n", blockSize, standardAvg, transposedAvg, improvement);
                }
            }

            System.out.println("Cache efficiency results saved to " + resultsDir + fileName);
        } catch (IOException e) {
            System.err.println("Error writing cache efficiency results: " + e.getMessage());
        }
    }

    private static void runScalabilityTest(String resultsDir, String methodType, String subMethod) {
        int[] sizes = {500, 1000, 2000, 3000}; // For sequential test
        int iterations = 3;

        if (methodType.equalsIgnoreCase("Sequential")) {
            System.out.println("\nRunning scalability test (Sequential Execution)...");

            try (PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/scalability-test-sequential.csv"))) {
                writer.println("Size,Time(ms)");

                for (int size : sizes) {
                    System.out.println("Testing matrix size: " + size);
                    double totalTime = 0;

                    for (int i = 0; i < iterations; i++) {
                        System.out.print("  Iteration " + (i + 1) + "... ");

                        System.gc();

                        Timer timer = new Timer();
                        Matrix A = new Matrix(size, size);
                        A.assignRandom();
                        Matrix B = new Matrix(size, size);
                        B.assignRandom();
                        Matrix C = new Matrix(size, size);

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

        } else if (methodType.equalsIgnoreCase("Parallel")) {
            System.out.println("\nRunning scalability test (Parallel Execution)...");

            int size = 2000;
            int maxThreads = Runtime.getRuntime().availableProcessors() * 2;

            try (PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/scalability-test.csv"))) {
                writer.println("Threads,Time(ms),Speedup,Efficiency(%)");

                System.out.println("Generating " + size + "x" + size + " matrices...");
                double[][] A = MatrixMultiplier.generateRandomMatrix(size, size);
                double[][] B = MatrixMultiplier.generateRandomMatrix(size, size);

                double baselineTime = 0;
                System.out.println("Testing single-threaded performance (baseline)...");

                for (int i = 0; i < iterations; i++) {
                    System.out.print("  Iteration " + (i + 1) + "... ");
                    System.gc();

                    long startTime = System.nanoTime();
                    MatrixMultiplier.multiplyMatrices(A, B, 1);
                    long endTime = System.nanoTime();

                    double executionTime = (endTime - startTime) / 1_000_000.0;
                    baselineTime += executionTime;

                    System.out.println(executionTime + " ms");
                }

                baselineTime /= iterations;
                writer.printf("1,%.2f,%.2f,%.2f\n", baselineTime, 1.0, 100.0);
                System.out.println("Baseline (1 thread): " + baselineTime + " ms");

                for (int threads = 2; threads <= maxThreads; threads++) {
                    System.out.println("Testing with " + threads + " threads...");
                    double totalTime = 0;

                    for (int i = 0; i < iterations; i++) {
                        System.out.print("  Iteration " + (i + 1) + "... ");
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

                System.out.println("Scalability test results saved to " + resultsDir + "/scalability-test.csv");

            } catch (IOException e) {
                System.err.println("Error writing scalability results: " + e.getMessage());
            }
        } else {
            System.out.println("Scalability test is not applicable for method type: " + methodType);
        }
    }


    private static void runThresholdOptimizationTest(String resultsDir, String methodType, String subMethod) {
        int size = 2000;
        int[] thresholds = {32, 64, 128, 256, 512, 1024};
        int iterations = 3;

        if (methodType.equalsIgnoreCase("Sequential")) {
            System.out.println("\nRunning threshold optimization test (Sequential Execution)...");

            try (PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/threshold-optimization-sequential.csv"))) {
                writer.println("Threshold,Time(ms)");

                Timer timer = new Timer();

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
                        System.gc();
                        timer.start();
                        Matrix C = A.multiplication(transposedB);  // Sequential method
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

        } else if (methodType.equalsIgnoreCase("Parallel")) {
            System.out.println("\nRunning threshold optimization test (Parallel Execution)...");

            int threads = Runtime.getRuntime().availableProcessors();

            try (PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/threshold-optimization-parallel.csv"))) {
                writer.println("Threshold,Time(ms)");

                System.out.println("Generating " + size + "x" + size + " matrices...");
                double[][] A = MatrixMultiplier.generateRandomMatrix(size, size);
                double[][] B = MatrixMultiplier.generateRandomMatrix(size, size);
                double[][] transposedB = MatrixMultiplier.transpose(B);

                for (int threshold : thresholds) {
                    System.out.println("Testing threshold: " + threshold);
                    double totalTime = 0;

                    for (int i = 0; i < iterations; i++) {
                        System.out.print("  Iteration " + (i + 1) + "... ");
                        System.gc();
                        double[][] C = new double[size][size];

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
                        + resultsDir + "/threshold-optimization-parallel.csv");

            } catch (IOException e) {
                System.err.println("Error writing threshold results: " + e.getMessage());
            }

        } else {
            System.out.println("Threshold optimization test is not applicable for method: " + methodType);
        }
    }

    private static void runMemoryAnalysis(String resultsDir, String methodType, String subMethod) {
            System.out.println("\nRunning memory analysis...");

            if (methodType.equalsIgnoreCase("Sequential")) {
                // Sequential memory profiling
                MatrixMemoryProfiler.runMemoryComparisonTestsSequential();
                MatrixMemoryProfiler.profileMatrixMultiplicationSequential(1500);

            } else if (methodType.equalsIgnoreCase("Parallel")) {
                // Parallel memory profiling
                Version3.MatrixMemoryProfiler.runMemoryComparisonTests();
                Version3.MatrixMemoryProfiler.profileMatrixMultiplication(
                        1500, Runtime.getRuntime().availableProcessors());

            } else {
                System.out.println("Invalid method type for memory analysis: " + methodType);
                System.out.println("Please select either 'Sequential' or 'Parallel'.");
                return;
            }

            System.out.println("Memory analysis complete");
    }

    private static void runQuickTest(String methodType, String subMethod) throws InterruptedException {
        System.out.println("\nRunning quick test with 500x500 matrix...");

        int size = 500;
        int threads = Runtime.getRuntime().availableProcessors();

        Matrix A = new Matrix(size, size);
        Matrix B = new Matrix(size, size);
        A.assignRandom();
        B.assignRandom();

        if (methodType.equalsIgnoreCase("Sequential")) {
            timer.start();
            Matrix result = A.multiplication(B);
            long elapsedTime = timer.end();

            System.out.println("Sequential multiplication time: " + elapsedTime + " ms");
            printSample(result.matrix);

        }
        else if (methodType.equalsIgnoreCase("Concurrent")) {
            Matrix result = null;
            long startTime, endTime;

            switch (subMethod) {
                case "MultipleThreads":
                    System.out.println("Multiplying using Multiple Threads (" + threads + " threads)...");
                    startTime = System.nanoTime();
                    result = MultiplyWithThreads.multiplyWithThreads(A, B);
                    endTime = System.nanoTime();
                    break;

                case "RowPerThread":
                    System.out.println("Multiplying using ThreadPool (Row per Thread)...");
                    startTime = System.nanoTime();
                    result = MultiplyWithThreadPool.assignPerRow(A, B);
                    endTime = System.nanoTime();
                    break;

                case "ChunkPerThread":
                    System.out.println("Multiplying using ThreadPool (Chunk per Thread)...");
                    startTime = System.nanoTime();
                    result = MultiplyWithThreadPool.assignPerChunk(A, B);
                    endTime = System.nanoTime();
                    break;

                default:
                    System.out.println("Unknown concurrent method. Skipping...");
                    return;
            }

            System.out.printf("Multiplication completed in %.2f ms\n", (endTime - startTime) / 1_000_000.0);
            printSample(result.matrix);
        }
        else if (methodType.equalsIgnoreCase("Parallel")) {
            System.out.println("Multiplying using ForkJoinPool (Parallel) with " + threads + " threads...");
            double[][] result;
            long startTime = System.nanoTime();
            result = MatrixMultiplier.multiplyMatrices(A.matrix, B.matrix, threads);
            long endTime = System.nanoTime();

            System.out.printf("Parallel multiplication completed in %.2f ms\n", (endTime - startTime) / 1_000_000.0);
            printSample(result);

        }
        else {
            System.out.println("Invalid method selected.");
        }
    }

    private static void printSample(double[][] matrix) {
        System.out.println("Sample of result matrix (top-left 3x3):");
        for (int i = 0; i < Math.min(3, matrix.length); i++) {
            for (int j = 0; j < Math.min(3, matrix[0].length); j++) {
                System.out.printf("%.2f\t", matrix[i][j]);
            }
            System.out.println();
        }
    }

}