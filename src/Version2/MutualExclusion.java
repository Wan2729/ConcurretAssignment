package Version2;
import Configuration.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MutualExclusion {
    public static void main(String[] args) throws InterruptedException {
        Timer timer = new Timer();

        /*
        Using Thread only
         */
        System.out.println("Multiple with Threads");
        // 500x500 start
        testMultiplyWithThreads(500, 500, timer);

        // 1000x1000 start
        testMultiplyWithThreads(1000, 1000, timer);

        // 5000x5000 start
        testMultiplyWithThreads(5000, 5000, timer);


        /*
        Using Thread Pool
         */
        // 500x500 start
//        testMultiplyWithThreadPool(500, 500, timer);
//
//        // 1000x1000 start
//        testMultiplyWithThreadPool(1000, 1000, timer);
//
//        // 5000x5000 start
//        testMultiplyWithThreadPool(5000, 5000, timer);
    }

    private static void testMultiplyWithThreads(int row, int col, Timer timer) throws InterruptedException {
        long elapsedTime;
        Matrix a2 = new Matrix(row, col);
        Matrix b2 = new Matrix(row, col);
        a2.assignRandom();
        b2.assignRandom();

        timer.start();
        multiplyWithThreadPool(a2, b2);
        elapsedTime = timer.end();
        System.out.println(row +"*" +col +" Time taken with Threads: " + elapsedTime + " ms");
    }

    private static void testMultiplyWithThreadPool(int row, int col, Timer timer) throws InterruptedException {
        long elapsedTime;
        Matrix a2 = new Matrix(row, col);
        Matrix b2 = new Matrix(row, col);
        a2.assignRandom();
        b2.assignRandom();

        timer.start();
        multiplyWithThreads(a2, b2);
        elapsedTime = timer.end();
        System.out.println(row +"*" +col +" Time taken with Thread pool: " + elapsedTime + " ms");
    } //

//    public static Matrix multiplyWithThreads(Matrix A, Matrix B) throws InterruptedException {
//        if (A.col != B.row) return null;
//        Matrix result = new Matrix(A.row, B.col);
//
//        Thread[] threads = new Thread[A.row];
//
//        for (int i = 0; i < A.row; i++) {
//            final int row = i;
//            threads[i] = new Thread(() -> {
//                for (int j = 0; j < B.col; j++) {
//                    double sum = 0;
//                    for (int k = 0; k < A.col; k++) {
//                        sum += A.matrix[row][k] * B.matrix[k][j];
//                    }
//                    synchronized (result) {
//                        result.matrix[row][j] = sum;
//                    }
//                }
//            });
//            threads[i].start();
//        }
//
//        for (Thread t : threads) {
//            t.join();
//        }
//
//        return result;
//    }

    public static Matrix multiplyWithThreads(Matrix A, Matrix B) throws InterruptedException {
        if (A.col != B.row) return null;
        Matrix result = new Matrix(A.row, B.col);
        Thread[] threads = new Thread[A.row];


        for (int row = 0; row < A.row; row++) {
            threads[row] = new Thread(new MatrixMultiplicationRunnable(A, B, result, row));
            threads[row].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        return result;
    }

    public static Matrix multiplyWithThreadPool(Matrix A, Matrix B) throws InterruptedException {
        if(A.col != B.row) return null;
        Matrix result = new Matrix(A.row, B.col);

        int threads = Runtime.getRuntime().availableProcessors(); //Get numbers of threads/cores available for this device
        ExecutorService executor = Executors.newFixedThreadPool(threads); //Create a new fixed thread pool use all available cores

        for(int row=0 ; row<A.row ; row++){
            executor.execute(new MatrixMultiplicationRunnable(A, B, result, row));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        return result;
    }
}
