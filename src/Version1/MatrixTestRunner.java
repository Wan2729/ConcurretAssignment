package Version1;

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
        System.out.println("Matrix Multiplication Performance Analysis");
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
//                    case 3:
//                        runMemoryAnalysis(resultsDir);
//                        break;
//                    case 4:
//                        runThresholdOptimizationTest(resultsDir);
//                        break;
//                    case 5:
//                        runScalabilityTest(resultsDir);
//                        break;
//                    case 6:
//                        runCustomTest(scanner, resultsDir);
//                        break;
//                    case 7:
//                        runCacheEfficiencyTest(resultsDir);
//                        break;
//                    case 8:
//                        runAllTests(resultsDir);
//                        break;
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
//        System.out.println("3. Memory Analysis");
//        System.out.println("4. Threshold Optimization Test");
//        System.out.println("5. Scalability Test (threads vs. speedup)");
//        System.out.println("6. Custom Test");
//        System.out.println("7. Cache Efficiency Test");
//        System.out.println("8. Run All Tests");
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
            }

            System.out.println("Benchmark results saved to " + resultsDir + "/comprehensive-benchmark.csv");

        } catch (IOException e) {
            System.err.println("Error writing benchmark results: " + e.getMessage());
        }
    }
}
