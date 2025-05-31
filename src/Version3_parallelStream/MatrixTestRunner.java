package Version3_parallelStream;

import Configuration.Matrix;
import Configuration.Memory;
import Configuration.Timer;

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

    public static void main(String[] args) {
        System.out.println("Matrix Parallel Stream Multiplication Performance Analysis");
        System.out.println("========================================");
        System.out.println("System Information:");
        System.out.println("  Processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("  Max Memory: " + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + " MB");
        System.out.println("  Java Version: " + System.getProperty("java.version"));
        System.out.println("  OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));

        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String resultsDir = "Version3-parallelStream-matrix-results-" + timestamp;
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
                    case 1 -> runQuickTest();
                    case 2 -> runComprehensiveBenchmark(resultsDir);
                    case 9 -> System.out.println("Exiting...");
                    default -> System.out.println("Invalid choice, please try again.");
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
        System.out.println("9. Exit");
        System.out.print("Enter your choice: ");
    }

    public static void runQuickTest() {
        Matrix A = new Matrix(500, 500);
        Matrix B = new Matrix(500, 500);
        A.assignRandom();
        B.assignRandom();

        timer.start();
        Matrix result = MatrixParallelStream.multiply(A, B);
        long elapsedTime = timer.end();

        System.out.println(500 + "x" + 500 + " Time taken with Parallel Stream: " + elapsedTime + " ms");
    }

    private static void runComprehensiveBenchmark(String resultsDir) {
        int[] sizes = {500, 1000, 5000};

        try (PrintWriter writer = new PrintWriter(new FileWriter(resultsDir + "/comprehensive-benchmark.csv"))) {
            writer.println("Size,Threads,Time(ms),Memory(MB),CPUUtilization(%),Speedup,Efficiency(%)");

            for (int size : sizes) {
                System.out.println("Testing matrix size: " + size + "x" + size);
                Matrix A = new Matrix(size, size);
                Matrix B = new Matrix(size, size);
                A.assignRandom();
                B.assignRandom();

                // Run sequential to get baseline
                long sequentialTime = getSequentialTime(size);

                System.gc();
                memory.start();
                timer.start();
                Matrix result = MatrixParallelStream.multiply(A, B);
                long parallelTime = timer.end();
                long usedMemory = memory.end();

                double speedup = (double) sequentialTime / parallelTime;
                int cores = Runtime.getRuntime().availableProcessors();
                double efficiency = (speedup / cores) * 100;

                System.out.printf("Parallel time: %d ms | Speedup: %.2f | Efficiency: %.2f%% | Memory: %d MB\n",
                        parallelTime, speedup, efficiency, usedMemory);

                writer.printf("%d,%s,%d,%d,%.2f,%.2f,%.2f\n",
                        size, "ParallelStream", parallelTime, usedMemory, 100.0, speedup, efficiency);
            }

            System.out.println("Benchmark results saved to " + resultsDir + "/comprehensive-benchmark.csv");

        } catch (IOException e) {
            System.err.println("Error writing benchmark results: " + e.getMessage());
        }
    }

    private static long getSequentialTime(int size) {
        Matrix A = new Matrix(size, size);
        Matrix B = new Matrix(size, size);
        A.assignRandom();
        B.assignRandom();

        System.gc();
        timer.start();
        A.multiplication(B); // built-in sequential method
        return timer.end();
    }
}
