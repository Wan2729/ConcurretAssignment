package Version3;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Simple benchmark for matrix multiplication without JMH dependencies
 */
public class SimpleMatrixBenchmark {

    // Test parameters
    private static final int[] MATRIX_SIZES = {500, 1000, 2000};
    private static final int[] THREAD_COUNTS = {1, 2, 4, Runtime.getRuntime().availableProcessors()};
    private static final int[] THRESHOLDS = {64, 128, 256, 512};
    private static final int[] BLOCK_SIZES = {16, 32, 64, 128};
    private static final int WARM_UP_ITERATIONS = 2;
    private static final int MEASUREMENT_ITERATIONS = 3;

    public static void main(String[] args) {
        System.out.println("Matrix Multiplication Benchmark");
        System.out.println("==============================");
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());

        // 1. Standard benchmark comparing matrix sizes and thread counts
        benchmarkSizesAndThreads();

        // 2. Threshold optimization benchmark
        benchmarkThresholds();

        // 3. Block size optimization benchmark
        benchmarkBlockSizes();

        // 4. Test transposed vs. non-transposed matrices
        benchmarkTranspose();
    }

    /**
     * Benchmark different matrix sizes and thread counts
     */
    private static void benchmarkSizesAndThreads() {
        System.out.println("\n=== Matrix Size and Thread Count Benchmark ===");
        System.out.println("Size\tThreads\tTime(ms)\tMemory(MB)");

        for (int size : MATRIX_SIZES) {
            double singleThreadTime = 0;

            for (int threads : THREAD_COUNTS) {
                // Warm up
                for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
                    runTest(size, threads);
                }

                // Actual measurement
                double totalTime = 0;
                long totalMemory = 0;

                for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
                    // Force GC
                    System.gc();

                    // Get initial memory
                    Runtime runtime = Runtime.getRuntime();
                    long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

                    // Run test
                    long startTime = System.nanoTime();
                    runTest(size, threads);
                    long endTime = System.nanoTime();

                    // Calculate metrics
                    double time = (endTime - startTime) / 1_000_000.0;
                    totalTime += time;

                    // Memory after
                    long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
                    totalMemory += (memoryAfter - memoryBefore) / (1024 * 1024);
                }

                // Calculate averages
                double avgTime = totalTime / MEASUREMENT_ITERATIONS;
                double avgMemory = (double) totalMemory / MEASUREMENT_ITERATIONS;

                // Store single-threaded time for speedup calculation
                if (threads == 1) {
                    singleThreadTime = avgTime;
                }

                // Calculate speedup
                double speedup = threads > 1 ? singleThreadTime / avgTime : 1.0;
                double efficiency = (speedup / threads) * 100.0;

                System.out.printf("%d\t%d\t%.2f\t\t%.2f\t(Speedup: %.2fx, Efficiency: %.2f%%)\n",
                        size, threads, avgTime, avgMemory, speedup, efficiency);
            }
            System.out.println();
        }
    }

    /**
     * Benchmark different threshold values
     */
    private static void benchmarkThresholds() {
        System.out.println("\n=== Threshold Optimization Benchmark ===");
        System.out.println("Threshold\tTime(ms)");

        int size = 2000;
        int threads = Runtime.getRuntime().availableProcessors();

        // Generate matrices once for all threshold tests
        double[][] A = MatrixMultiplier.generateRandomMatrix(size, size);
        double[][] B = MatrixMultiplier.generateRandomMatrix(size, size);
        double[][] transposedB = MatrixMultiplier.transpose(B);

        for (int threshold : THRESHOLDS) {
            // Warm up
            for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
                runCustomTest(A, transposedB, threads, threshold, 32, true);
            }

            // Measure
            double totalTime = 0;
            for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
                long startTime = System.nanoTime();
                runCustomTest(A, transposedB, threads, threshold, 32, true);
                long endTime = System.nanoTime();

                double time = (endTime - startTime) / 1_000_000.0;
                totalTime += time;
            }

            double avgTime = totalTime / MEASUREMENT_ITERATIONS;
            System.out.printf("%d\t\t%.2f\n", threshold, avgTime);
        }
    }

    /**
     * Benchmark different block sizes
     */
    private static void benchmarkBlockSizes() {
        System.out.println("\n=== Block Size Optimization Benchmark ===");
        System.out.println("BlockSize\tTime(ms)");

        int size = 2000;
        int threads = Runtime.getRuntime().availableProcessors();

        // Generate matrices once for all block size tests
        double[][] A = MatrixMultiplier.generateRandomMatrix(size, size);
        double[][] B = MatrixMultiplier.generateRandomMatrix(size, size);
        double[][] transposedB = MatrixMultiplier.transpose(B);

        for (int blockSize : BLOCK_SIZES) {
            // Warm up
            for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
                runCustomTest(A, transposedB, threads, 128, blockSize, true);
            }

            // Measure
            double totalTime = 0;
            for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
                long startTime = System.nanoTime();
                runCustomTest(A, transposedB, threads, 128, blockSize, true);
                long endTime = System.nanoTime();

                double time = (endTime - startTime) / 1_000_000.0;
                totalTime += time;
            }

            double avgTime = totalTime / MEASUREMENT_ITERATIONS;
            System.out.printf("%d\t\t%.2f\n", blockSize, avgTime);
        }
    }

    /**
     * Benchmark transposed vs. non-transposed matrices
     */
    private static void benchmarkTranspose() {
        System.out.println("\n=== Transpose Optimization Benchmark ===");
        System.out.println("Size\tStandard(ms)\tTransposed(ms)\tImprovement(%)");

        int threads = Runtime.getRuntime().availableProcessors();

        for (int size : MATRIX_SIZES) {
            // Generate matrices
            double[][] A = MatrixMultiplier.generateRandomMatrix(size, size);
            double[][] B = MatrixMultiplier.generateRandomMatrix(size, size);
            double[][] transposedB = MatrixMultiplier.transpose(B);

            // Test standard multiplication (no transpose)
            // Warm up
            for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
                runCustomTest(A, B, threads, 128, 32, false);
            }

            // Measure standard
            double standardTotalTime = 0;
            for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
                long startTime = System.nanoTime();
                runCustomTest(A, B, threads, 128, 32, false);
                long endTime = System.nanoTime();

                double time = (endTime - startTime) / 1_000_000.0;
                standardTotalTime += time;
            }
            double standardAvgTime = standardTotalTime / MEASUREMENT_ITERATIONS;

            // Test transposed multiplication
            // Warm up
            for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
                runCustomTest(A, transposedB, threads, 128, 32, true);
            }

            // Measure transposed
            double transposedTotalTime = 0;
            for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
                long startTime = System.nanoTime();
                runCustomTest(A, transposedB, threads, 128, 32, true);
                long endTime = System.nanoTime();

                double time = (endTime - startTime) / 1_000_000.0;
                transposedTotalTime += time;
            }
            double transposedAvgTime = transposedTotalTime / MEASUREMENT_ITERATIONS;

            // Calculate improvement
            double improvement = ((standardAvgTime - transposedAvgTime) / standardAvgTime) * 100;

            System.out.printf("%d\t%.2f\t\t%.2f\t\t%.2f%%\n",
                    size, standardAvgTime, transposedAvgTime, improvement);
        }
    }

    /**
     * Run standard matrix multiplication test
     */
    private static void runTest(int size, int threads) {
        // Generate matrices
        double[][] A = MatrixMultiplier.generateRandomMatrix(size, size);
        double[][] B = MatrixMultiplier.generateRandomMatrix(size, size);

        // Multiply
        MatrixMultiplier.multiplyMatrices(A, B, threads);
    }

    /**
     * Run custom matrix multiplication test with specified parameters
     */
    private static void runCustomTest(double[][] A, double[][] B, int threads,
            int threshold, int blockSize, boolean isTransposed) {
        // Create result matrix
        double[][] C = new double[A.length][B[0].length];

        // Use custom task with specified parameters
        ForkJoinPool pool = new ForkJoinPool(threads);
        pool.invoke(new CustomThresholdTask(A, B, C, 0, A.length, threshold, blockSize, isTransposed));
        pool.shutdown();
    }

    /**
     * Custom matrix multiplication task with configurable parameters
     */
    public static class CustomThresholdTask extends RecursiveAction {

        private final double[][] A, B, C;
        private final int startRow, endRow;
        private final int threshold;
        private final int blockSize;
        private final boolean isTransposed;

        public CustomThresholdTask(double[][] A, double[][] B, double[][] C,
                int startRow, int endRow,
                int threshold, int blockSize, boolean isTransposed) {
            this.A = A;
            this.B = B;
            this.C = C;
            this.startRow = startRow;
            this.endRow = endRow;
            this.threshold = threshold;
            this.blockSize = blockSize;
            this.isTransposed = isTransposed;
        }

        @Override
        protected void compute() {
            int rows = endRow - startRow;

            if (rows <= threshold) {
                multiplyBlocked();
            } else {
                int mid = (startRow + endRow) / 2;
                CustomThresholdTask task1 = new CustomThresholdTask(
                        A, B, C, startRow, mid, threshold, blockSize, isTransposed);
                CustomThresholdTask task2 = new CustomThresholdTask(
                        A, B, C, mid, endRow, threshold, blockSize, isTransposed);
                invokeAll(task1, task2);
            }
        }

        private void multiplyBlocked() {
            final int n = C[0].length;
            final int k = A[0].length;

            for (int i0 = startRow; i0 < endRow; i0 += blockSize) {
                int iLimit = Math.min(i0 + blockSize, endRow);

                for (int j0 = 0; j0 < n; j0 += blockSize) {
                    int jLimit = Math.min(j0 + blockSize, n);

                    for (int k0 = 0; k0 < k; k0 += blockSize) {
                        int kLimit = Math.min(k0 + blockSize, k);

                        for (int i = i0; i < iLimit; i++) {
                            for (int j = j0; j < jLimit; j++) {
                                double sum = C[i][j];

                                if (isTransposed) {
                                    for (int kk = k0; kk < kLimit; kk++) {
                                        sum += A[i][kk] * B[j][kk];
                                    }
                                } else {
                                    for (int kk = k0; kk < kLimit; kk++) {
                                        sum += A[i][kk] * B[kk][j];
                                    }
                                }

                                C[i][j] = sum;
                            }
                        }
                    }
                }
            }
        }
    }
}
