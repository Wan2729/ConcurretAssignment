package Version2;
import Configuration.*;

public class MultiplyWithThreads {
    public static void main(String[] args) throws InterruptedException {
        Timer timer = new Timer();

        /*
        Using Threads
         */
        System.out.println("Multiple with Threads");
        // 500x500 start
        testMultiplyWithThreads(500, 500, timer);

        // 1000x1000 start
        testMultiplyWithThreads(1000, 1000, timer);

        // 5000x5000 start
        testMultiplyWithThreads(5000, 5000, timer);
    }

    private static void testMultiplyWithThreads(int row, int col, Timer timer) throws InterruptedException {
        long elapsedTime;
        Matrix a2 = new Matrix(row, col);
        Matrix b2 = new Matrix(row, col);
        a2.assignRandom();
        b2.assignRandom();

        timer.start();
        multiplyWithThreads(a2, b2);
        elapsedTime = timer.end();
        System.out.println(row +"*" +col +" Time taken with Threads: " + elapsedTime + " ms");
    }

    /*
    Multiplication using Threads
     */
    public static Matrix multiplyWithThreads(Matrix A, Matrix B) throws InterruptedException {
        if (A.col != B.row) return null; //Return null kalau tak valid for multiplication
        Matrix result = new Matrix(A.row, B.col); // New matrix to hold result of multiplication
        Thread[] threads = new Thread[A.row]; // Create array of threads with the size of first array's row

        for (int row = 0; row < A.row; row++) { // Iterate each row of first array
            /*
            Run every task as lambda expression
             */
            final int i = row;
            threads[row] = new Thread(() -> {
                for (int j = 0; j < B.col; j++) {
                    double sum=0;
                    for (int k = 0; k < A.col; k++) {
                        sum += A.matrix[i][k] * B.matrix[k][j];
                    }
                    result.matrix[i][j] = sum;
                }
            });
            threads[row].start(); // Start the thread
        }

        for (Thread t : threads) {
            t.join(); // Wait until every thread completed
        }

        return result;
    }
}
