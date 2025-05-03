package Version2;
import Configuration.*;

public class MutualExclusion {
    public static void main(String[] args) throws InterruptedException {
        Matrix result;
        Timer timer = new Timer();
        long elapsedTime;

        // 500x500 start
        Matrix a1 = new Matrix(500, 500);
        Matrix b1 = new Matrix(500, 500);
        a1.assignRandom();
        b1.assignRandom();

        timer.start();
        result = multiplyWithThreads(a1, b1);
        elapsedTime = timer.end();
        System.out.println("500x500 Time taken with mutual exclusion: " + elapsedTime + " ms");

        // 1000x1000 start
        Matrix a2 = new Matrix(1000, 1000);
        Matrix b2 = new Matrix(1000, 1000);
        a2.assignRandom();
        b2.assignRandom();

        timer.start();
        result = multiplyWithThreads(a2, b2);
        elapsedTime = timer.end();
        System.out.println("1000x1000 Time taken with mutual exclusion: " + elapsedTime + " ms");

        // 5000x5000 start
        Matrix a3 = new Matrix(5000, 5000);
        Matrix b3 = new Matrix(5000, 5000);
        a3.assignRandom();
        b3.assignRandom();

        timer.start();
        result = multiplyWithThreads(a3, b3);
        elapsedTime = timer.end();
        System.out.println("5000x5000 Time taken with mutual exclusion: " + elapsedTime + " ms");
    }

    public static Matrix multiplyWithThreads(Matrix A, Matrix B) throws InterruptedException {
        if (A.col != B.row) return null;
        Matrix result = new Matrix(A.row, B.col);

        Thread[] threads = new Thread[A.row];

        for (int i = 0; i < A.row; i++) {
            final int row = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < B.col; j++) {
                    double sum = 0;
                    for (int k = 0; k < A.col; k++) {
                        sum += A.matrix[row][k] * B.matrix[k][j];
                    }
                    synchronized (result) {
                        result.matrix[row][j] = sum;
                    }
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        return result;
    }
}
