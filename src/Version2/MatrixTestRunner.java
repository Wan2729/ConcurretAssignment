package Version2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.ForkJoinPool;

public class MatrixTestRunner {
    final static Timer timer = new Timer();

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
    }

    /**
     * Run comprehensive benchmark with various matrix sizes
     */
    private static void runComprehensiveBenchmark(String resultsDir) {
        System.out.println("\nRunning comprehensive benchmark...");
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
        runQuickTest();
        runComprehensiveBenchmark(resultsDir);
        runMemoryAnalysis(resultsDir);
        runThresholdOptimizationTest(resultsDir);
        runScalabilityTest(resultsDir);
        runCacheEfficiencyTest(resultsDir);

        System.out.println("\nAll tests completed. Results saved to " + resultsDir);
    }
}
