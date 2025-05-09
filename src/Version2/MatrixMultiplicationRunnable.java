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
        for(int i=0 ; i< B.col ; i++){
            double sum = 0;
            for(int j=0 ; j< A.col ; j++){
                result.matrix[rowA][i] += A.matrix[rowA][j] * B.matrix[j][i];
            }
        }
    }
}
