package Version2;

import Configuration.Matrix;

public class MatrixMultiplicationRunnable implements Runnable{
    private final Matrix A;
    private final Matrix B;
    private final Matrix result;
    private final int rowA;

    public MatrixMultiplicationRunnable(Matrix A, Matrix B, Matrix result, int rowA) {
        this.rowA = rowA;
        this.A = A;
        this.B = B;
        this.result = result;
    }

    @Override
    public void run() {
        for(int i=0 ; i< B.col ; i++){ // iterate every matrix B's columns
            double sum=0;
            for(int j=0 ; j< A.col ; j++){ // iterate again for every matrix A's column to do multiplication
                sum += A.matrix[rowA][j] * B.matrix[j][i]; // Multiply the array and sum up to its result's row col
            }
            result.matrix[rowA][i] = sum;
        }
    }
}
