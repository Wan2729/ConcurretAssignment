package version3.parallel.Forkjoin;

import java.util.concurrent.RecursiveAction;

public class MatrixMultiplyTask extends RecursiveAction {

    // Tuning parameters - adjust these based on your hardware
    private static final int THRESHOLD = 128; // Default threshold - can be optimized per system
    private static final int BLOCK_SIZE = 32; // For cache-friendly blocked multiplication

    private final double[][] A, B, C;
    private final int startRow, endRow;

    public MatrixMultiplyTask(double[][] A, double[][] B, double[][] C, int startRow, int endRow) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    @Override
    protected void compute() {
        if ((endRow - startRow) <= THRESHOLD) {
            // Small enough chunk - use cache-friendly blocked multiplication
            multiplyBlocked();
        } else {
            // Split into smaller tasks
            int mid = (startRow + endRow) / 2;
            MatrixMultiplyTask task1 = new MatrixMultiplyTask(A, B, C, startRow, mid);
            MatrixMultiplyTask task2 = new MatrixMultiplyTask(A, B, C, mid, endRow);
            invokeAll(task1, task2);
        }
    }

    /**
     * Cache-friendly blocked matrix multiplication implementation This
     * significantly improves performance by better utilizing CPU cache
     */
    private void multiplyBlocked() {
        final int n = A.length;
        final int m = B[0].length;
        final int k = A[0].length;

        // Process assigned rows only
        for (int i = startRow; i < endRow; i++) {
            // Process blocks of columns and inner dimension for better cache locality
            for (int jj = 0; jj < m; jj += BLOCK_SIZE) {
                for (int kk = 0; kk < k; kk += BLOCK_SIZE) {
                    // Bounds for current block
                    int jEnd = Math.min(jj + BLOCK_SIZE, m);
                    int kEnd = Math.min(kk + BLOCK_SIZE, k);

                    // Process current block
                    for (int j = jj; j < jEnd; j++) {
                        for (int kk2 = kk; kk2 < kEnd; kk2++) {
                            // Load B[kk2][j] once for this iteration
                            double bVal = B[kk2][j];
                            // Inner product with A's row
                            if (bVal != 0) { // Skip multiplications by zero
                                C[i][j] += A[i][kk2] * bVal;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Standard matrix multiplication - less cache-efficient but simpler Keep
     * this for comparison or if the blocked version has issues
     */
    private void multiplyStandard() {
        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < B[0].length; j++) {
                double sum = 0.0; // Accumulate in local variable for better performance
                for (int k = 0; k < A[0].length; k++) {
                    sum += A[i][k] * B[k][j];
                }
                C[i][j] = sum; // Single write to C[i][j]
            }
        }
    }
}
