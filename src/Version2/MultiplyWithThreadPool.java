package Version2;

import Configuration.Matrix;
import Configuration.Timer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MultiplyWithThreadPool {
    public static void main(String[] args) throws InterruptedException {
        Timer timer = new Timer();

        /*
        Assign Task Per Row
         */
//        System.out.println("Multiple with Thread Pools (Assign Task Per Row)");
//        // 500x500 start
//        testThreadPoolPerRowTask(500, 500, timer);
//
//        // 1000x1000 start
//        testThreadPoolPerRowTask(1000, 1000, timer);
//
//        // 5000x5000 start
//        testThreadPoolPerRowTask(5000, 5000, timer);

        /*
        Assign Task Per Element
         */
//        System.out.println("Multiple with Thread Pools (Assign Task Per Element)");
//        // 500x500 start
//        testThreadPoolPerElementTask(500, 500, timer);
//
//        // 1000x1000 start
//        testThreadPoolPerElementTask(1000, 1000, timer);
//
//        // 5000x5000 start
//        testThreadPoolPerElementTask(5000, 5000, timer);

        /*
        Assign Task Per Chunk
         */
        System.out.println("Multiple with Thread Pools (Assign Task Per Chunk)");
        // 500x500 start
        testThreadPoolPerChunkTask(500, 500, timer);

        // 1000x1000 start
        testThreadPoolPerChunkTask(1000, 1000, timer);

        // 5000x5000 start
        testThreadPoolPerChunkTask(5000, 5000, timer);
    }

    private static void testThreadPoolPerRowTask(int row, int col, Timer timer) throws InterruptedException {
        long elapsedTime;
        Matrix a2 = new Matrix(row, col);
        Matrix b2 = new Matrix(row, col);
        a2.assignRandom();
        b2.assignRandom();

        timer.start();
        assignPerRow(a2, b2);
        elapsedTime = timer.end();
        System.out.println(row +"*" +col +" Time taken with Thread pool: " + elapsedTime + " ms");
    }

    private static void testThreadPoolPerChunkTask(int row, int col, Timer timer) throws InterruptedException {
        long elapsedTime;
        Matrix a2 = new Matrix(row, col);
        Matrix b2 = new Matrix(row, col);
        a2.assignRandom();
        b2.assignRandom();

        timer.start();
        assignPerChunk(a2, b2);
        elapsedTime = timer.end();
        System.out.println(row +"*" +col +" Time taken with Thread pool: " + elapsedTime + " ms");
    }

    private static void testThreadPoolPerElementTask(int row, int col, Timer timer) throws InterruptedException {
        long elapsedTime;
        Matrix a2 = new Matrix(row, col);
        Matrix b2 = new Matrix(row, col);
        a2.assignRandom();
        b2.assignRandom();

        timer.start();
        assignPerElement(a2, b2);
        elapsedTime = timer.end();
        System.out.println(row +"*" +col +" Time taken with Thread pool: " + elapsedTime + " ms");
    }

    /*
    Assigning Per-Row Tasks
     */
    public static Matrix assignPerRow(Matrix A, Matrix B) throws InterruptedException {
        if(A.col != B.row) return null;
        Matrix result = new Matrix(A.row, B.col);

        int threads = Runtime.getRuntime().availableProcessors(); //Get numbers of threads/cores available for this device
        ExecutorService executor = Executors.newFixedThreadPool(threads); //Create a new fixed thread pool use all available cores

        for (int i = 0; i < A.row; i++) {
            final int row = i;
            executor.execute(() -> {
                for (int j = 0; j < B.col; j++) {
                    double sum=0;
                    for (int k = 0; k < A.col; k++) {
                        sum += A.matrix[row][k] * B.matrix[k][j];
                    }
                    result.matrix[row][j] = sum; // No need to synchronize if each thread writes to its own row
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        return result;
    }

    /*
    Assigning Per-Chunk Tasks
     */
    public static Matrix assignPerChunk(Matrix A, Matrix B) throws InterruptedException {
        if(A.col != B.row) return null;
        Matrix result = new Matrix(A.row, B.col);

        int threads = Runtime.getRuntime().availableProcessors(); //Get numbers of threads/cores available for this device
        final int chunkSize = (int) Math.ceil((double)A.row/threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads); //Create a new fixed thread pool use all available cores

        for(int task=0 ; task<chunkSize ; task++){
            final int startRow = task * chunkSize;
            final int endRow = Math.min(startRow + chunkSize, A.row);

            executor.execute(() -> {
                for(int row=startRow ; row<endRow ; row++){
                    for(int col=0 ; col<B.col ; col++){
                        double sum=0;
                        for(int j=0 ; j<A.col ; j++){
                            sum += A.matrix[row][j] * B.matrix[col][j];
                        }
                        result.matrix[row][col] = sum; // No need to synchronize if each thread writes to its own row
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        return result;
    }

    /*
    Assigning Per-Element Tasks
     */
    public static Matrix assignPerElement(Matrix A, Matrix B) throws InterruptedException {
        if(A.col != B.row) return null;
        Matrix result = new Matrix(A.row, B.col);

        int threads = Runtime.getRuntime().availableProcessors(); //Get numbers of threads/cores available for this device
        ExecutorService executor = Executors.newFixedThreadPool(threads); //Create a new fixed thread pool use all available cores

        for (int i = 0; i < A.row; i++) {
            for (int j = 0; j < B.col; j++) {
                final int row = i;
                final int col = j;
                executor.execute(() -> {
                    double sum=0;
                    for (int k = 0; k < A.col; k++) {
                        sum += A.matrix[row][k] * B.matrix[k][col];
                    }
                    result.matrix[row][col] = sum;
                });
            }
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        return result;
    }
}
