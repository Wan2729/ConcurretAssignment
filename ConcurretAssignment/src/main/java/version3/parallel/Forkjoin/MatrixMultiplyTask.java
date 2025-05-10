package version3.parallel.Forkjoin;

import java.util.concurrent.RecursiveAction;

/**
 * Enhanced MatrixMultiplyTask with adaptive parameters and improved cache
 * efficiency
 */
public class MatrixMultiplyTask extends RecursiveAction {

    // Adaptive parameters for different matrix sizes
    private static int determineThreshold(int matrixSize) {
        if (matrixSize <= 500) {
            return 64;
        }
        if (matrixSize <= 2000) {
            return 128;
        }
        return 256;
    }

    private static int determineBlockSize(int matrixSize) {
        if (matrixSize <= 1000) {
            return 32;
        }
        return 64;
    }

    // Instance variables
    private final double[][] A, B, C;
    private final int startRow, endRow;
    private final int threshold;
    private final int blockSize;
    private final boolean isTransposed;

    /**
     * Constructor for matrix multiplication task
     *
     * @param A First matrix
     * @param B Second matrix (or transposed second matrix)
     * @param C Result matrix
     * @param startRow Starting row index
     * @param endRow Ending row index (exclusive)
     * @param isTransposed Whether B is already transposed
     */
    public MatrixMultiplyTask(double[][] A, double[][] B, double[][] C,
            int startRow, int endRow, boolean isTransposed) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.startRow = startRow;
        this.endRow = endRow;
        this.isTransposed = isTransposed;

        // Compute adaptive parameters based on matrix size
        this.threshold = determineThreshold(A.length);
        this.blockSize = determineBlockSize(A.length);
    }

    /**
     * Legacy constructor without transpose flag
     */
    public MatrixMultiplyTask(double[][] A, double[][] B, double[][] C,
            int startRow, int endRow) {
        this(A, B, C, startRow, endRow, false);
    }

    @Override
    protected void compute() {
        int rows = endRow - startRow;

        if (rows <= threshold) {
            // Small enough chunk - compute directly
            multiplyBlockedOptimized();
        } else {
            // Split into smaller tasks
            int mid = (startRow + endRow) / 2;
            MatrixMultiplyTask task1 = new MatrixMultiplyTask(A, B, C, startRow, mid, isTransposed);
            MatrixMultiplyTask task2 = new MatrixMultiplyTask(A, B, C, mid, endRow, isTransposed);
            invokeAll(task1, task2);
        }
    }

    /**
     * Enhanced blocked matrix multiplication with better cache locality Uses
     * transposed or regular matrix B based on the isTransposed flag
     */
    private void multiplyBlockedOptimized() {
        final int n = C[0].length;  // Columns in result matrix
        final int k = A[0].length;  // Inner dimension (columns of A, rows of B)

        // Process each block in cache-friendly order
        for (int i0 = startRow; i0 < endRow; i0 += blockSize) {
            int iLimit = Math.min(i0 + blockSize, endRow);

            for (int j0 = 0; j0 < n; j0 += blockSize) {
                int jLimit = Math.min(j0 + blockSize, n);

                for (int k0 = 0; k0 < k; k0 += blockSize) {
                    int kLimit = Math.min(k0 + blockSize, k);

                    // Process current block
                    for (int i = i0; i < iLimit; i++) {
                        for (int j = j0; j < jLimit; j++) {
                            double sum = C[i][j]; // Load accumulated value

                            if (isTransposed) {
                                // If B is transposed, use B[j][k] instead of B[k][j]
                                for (int kk = k0; kk < kLimit; kk++) {
                                    sum += A[i][kk] * B[j][kk];
                                }
                            } else {
                                // Standard matrix multiplication
                                for (int kk = k0; kk < kLimit; kk++) {
                                    sum += A[i][kk] * B[kk][j];
                                }
                            }

                            C[i][j] = sum; // Store accumulated value
                        }
                    }
                }
            }
        }
    }

    /**
     * Alternative implementation using loop unrolling for potential SIMD
     * optimization This method can be faster on processors with good
     * vectorization support
     */
    private void multiplyUnrolled() {
        final int n = C[0].length;
        final int k = A[0].length;

        for (int i = startRow; i < endRow; i++) {
            for (int j = 0; j < n; j++) {
                // Accumulate in multiple independent variables for potential SIMD
                double sum0 = 0.0, sum1 = 0.0, sum2 = 0.0, sum3 = 0.0;

                // Process 4 elements at once
                int kLimit = (k / 4) * 4;
                for (int kk = 0; kk < kLimit; kk += 4) {
                    if (isTransposed) {
                        sum0 += A[i][kk] * B[j][kk];
                        sum1 += A[i][kk + 1] * B[j][kk + 1];
                        sum2 += A[i][kk + 2] * B[j][kk + 2];
                        sum3 += A[i][kk + 3] * B[j][kk + 3];
                    } else {
                        sum0 += A[i][kk] * B[kk][j];
                        sum1 += A[i][kk + 1] * B[kk + 1][j];
                        sum2 += A[i][kk + 2] * B[kk + 2][j];
                        sum3 += A[i][kk + 3] * B[kk + 3][j];
                    }
                }

                // Handle remaining elements
                for (int kk = kLimit; kk < k; kk++) {
                    if (isTransposed) {
                        sum0 += A[i][kk] * B[j][kk];
                    } else {
                        sum0 += A[i][kk] * B[kk][j];
                    }
                }

                C[i][j] = sum0 + sum1 + sum2 + sum3;
            }
        }
    }
}
