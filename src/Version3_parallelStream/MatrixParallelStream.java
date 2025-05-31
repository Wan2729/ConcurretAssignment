package Version3_parallelStream;

import Configuration.Matrix;

public class MatrixParallelStream {

    public static Matrix multiply(Matrix A, Matrix B) {
        if (A.getCols() != B.getRows()) {
            throw new IllegalArgumentException("Matrix dimensions do not match for multiplication.");
        }

        int rows = A.getRows();
        int cols = B.getCols();
        int common = A.getCols();

        Matrix result = new Matrix(rows, cols);

        java.util.stream.IntStream.range(0, rows).parallel().forEach(i -> {
            for (int j = 0; j < cols; j++) {
                double sum = 0; // use double since Matrix stores double
                for (int k = 0; k < common; k++) {
                    sum += A.getValue(i, k) * B.getValue(k, j);
                }
                result.setValue(i, j, sum);
            }
        });

        return result;
    }
}
