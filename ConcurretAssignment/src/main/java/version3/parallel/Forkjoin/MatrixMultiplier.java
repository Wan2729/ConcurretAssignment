package version3.parallel.Forkjoin;

import java.lang.management.ManagementFactory;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import com.sun.management.OperatingSystemMXBean;

/**
 * Enhanced MatrixMultiplier with improved performance metrics
 */
public class MatrixMultiplier {

    // Reusable ForkJoinPool to avoid creation overhead
    private static final ForkJoinPool DEFAULT_POOL = new ForkJoinPool(
            Runtime.getRuntime().availableProcessors());

    /**
     * Generates a random matrix with specified dimensions
     *
     * @param rows Number of rows
     * @param cols Number of columns
     * @return A randomly filled matrix
     */
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

    /**
     * Multiplies two matrices using ForkJoin parallelism with default pool
     *
     * @param A First matrix
     * @param B Second matrix
     * @return Result matrix C = A * B
     */
    public static double[][] multiplyMatrices(double[][] A, double[][] B) {
        // Validate matrix dimensions
        if (A[0].length != B.length) {
            throw new IllegalArgumentException("Matrix dimensions are incompatible for multiplication");
        }

        // Create result matrix
        double[][] C = new double[A.length][B[0].length];

        // Use transpose optimization for better cache locality
        double[][] transposedB = transpose(B);

        // Use the default pool
        DEFAULT_POOL.invoke(new MatrixMultiplyTask(A, transposedB, C, 0, A.length, true));

        return C;
    }

    /**
     * Multiplies two matrices using ForkJoin parallelism with specified number
     * of threads
     *
     * @param A First matrix
     * @param B Second matrix
     * @param numThreads Number of threads to use in the ForkJoinPool
     * @return Result matrix C = A * B
     */
    public static double[][] multiplyMatrices(double[][] A, double[][] B, int numThreads) {
        // Validate matrix dimensions
        if (A[0].length != B.length) {
            throw new IllegalArgumentException("Matrix dimensions are incompatible for multiplication");
        }

        // Create result matrix
        double[][] C = new double[A.length][B[0].length];

        // Transpose B for better cache performance
        double[][] transposedB = transpose(B);

        // Use custom ForkJoinPool size
        ForkJoinPool pool = new ForkJoinPool(numThreads);
        pool.invoke(new MatrixMultiplyTask(A, transposedB, C, 0, A.length, true));
        pool.shutdown();

        return C;
    }

    /**
     * Transpose a matrix for better cache performance
     *
     * @param matrix Original matrix
     * @return Transposed matrix
     */
    public static double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] result = new double[cols][rows];

        // Process in blocks for better cache behavior
        final int blockSize = 32;
        for (int i0 = 0; i0 < rows; i0 += blockSize) {
            int iLimit = Math.min(i0 + blockSize, rows);

            for (int j0 = 0; j0 < cols; j0 += blockSize) {
                int jLimit = Math.min(j0 + blockSize, cols);

                for (int i = i0; i < iLimit; i++) {
                    for (int j = j0; j < jLimit; j++) {
                        result[j][i] = matrix[i][j];
                    }
                }
            }
        }

        return result;
    }

    /**
     * Run a benchmark measuring time, memory and CPU usage
     *
     * @param size Matrix size
     * @param threads Number of threads
     * @return Performance metrics
     */
    public static BenchmarkResult runBenchmark(int size, int threads) {
        BenchmarkResult result = new BenchmarkResult();
        result.matrixSize = size;
        result.threadCount = threads;

        // Force garbage collection before test
        System.gc();

        // Measure memory before
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        // Get CPU time before
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
        long cpuTimeBefore = osBean.getProcessCpuTime();

        // Generate test matrices
        double[][] A = generateRandomMatrix(size, size);
        double[][] B = generateRandomMatrix(size, size);

        // Perform multiplication with time measurement
        ForkJoinPool pool = new ForkJoinPool(threads);
        long startTime = System.nanoTime();
        double[][] C = multiplyMatrices(A, B, threads);
        long endTime = System.nanoTime();

        // Record ForkJoin stats
        result.stealCount = pool.getStealCount();
        pool.shutdown();

        // Record time
        result.executionTime = (endTime - startTime) / 1_000_000.0; // Convert to ms

        // Record CPU usage
        long cpuTimeAfter = osBean.getProcessCpuTime();
        double cpuUsage = (double) (cpuTimeAfter - cpuTimeBefore) / (endTime - startTime);
        result.cpuUtilization = cpuUsage * 100.0; // As percentage

        // Force GC again
        System.gc();

        // Measure memory after
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        result.memoryUsed = (memoryAfter - memoryBefore) / (1024 * 1024); // Convert to MB

        return result;
    }

    /**
     * Benchmark result class
     */
    public static class BenchmarkResult {

        public int matrixSize;
        public int threadCount;
        public double executionTime; // ms
        public long memoryUsed;      // MB
        public double cpuUtilization; // %
        public long stealCount;

        @Override
        public String toString() {
            return String.format(
                    "Size: %d×%d, Threads: %d, Time: %.2f ms, Memory: %d MB, CPU: %.2f%%, Steals: %d",
                    matrixSize, matrixSize, threadCount, executionTime, memoryUsed,
                    cpuUtilization, stealCount);
        }
    }

    public static void main(String[] args) {
        // Example usage with performance measurement
        int[] sizes = {500, 1000, 2000};
        int[] threadCounts = {1, 2, 4, Runtime.getRuntime().availableProcessors()};

        System.out.println("Matrix Multiplication Benchmark");
        System.out.println("==============================");
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());

        for (int size : sizes) {
            System.out.println("\nTesting matrices of size " + size + "×" + size);
            System.out.println("----------------------------------------");

            for (int threads : threadCounts) {
                // Run warm-up
                runBenchmark(size, threads);

                // Run actual benchmark
                BenchmarkResult result = runBenchmark(size, threads);
                System.out.println(result);
            }
        }
    }
}
