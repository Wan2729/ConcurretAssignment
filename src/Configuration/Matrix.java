package Configuration;

import javax.swing.*;
import java.util.Random;

public class Matrix {
    public int row, col;
    public double[][] matrix;
    public JFrame frame;
    public JProgressBar progressBar;

    public Matrix(int row, int column){
        this.row = row;
        this.col = column;
        matrix = new double[row][column];
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
    }

    public void assignRandom(){
        Random random = new Random();
        for(int i=0; i<row; ++i ){
            for(int j=0; j<col; ++j ){
                matrix[i][j] = random.nextDouble(1000);
            }
        }
    }

    public Matrix multiplication(Matrix a) {
        if (this.col != a.row) {return null;}
        Matrix result = new Matrix(this.row, a.col);

        for (int i = 0; i < this.row; i++) {
            for (int j = 0; j < a.col; j++) {
                double sum = 0;
                for (int k = 0; k < this.col; k++) {
                    sum += this.matrix[i][k] * a.matrix[k][j]; // Multiply and accumulate
                }
                result.matrix[i][j] = sum; // Assign the computed value
            }

            // Update the progress bar
            int progress = (int) (((double) (i + 1) / this.row) * 100);
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(progress);
                progressBar.setString("Progress: " + progress + "%");
            });
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Matrix [").append(row).append("x").append(col).append("]\n");

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                sb.append(String.format("%.2f", matrix[i][j])).append(" ");
            }
            sb.append("\n"); // New line for each row
        }

        return sb.toString();
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public Matrix transpose() {
        Matrix transposed = new Matrix(this.col, this.row); // Swap row and column sizes

        for (int i = 0; i < this.row; i++) {
            for (int j = 0; j < this.col; j++) {
                transposed.matrix[j][i] = this.matrix[i][j]; // Swap indices
            }
        }

        return transposed;
    }

    public Matrix multiplicationBlocked(Matrix a, int blockSize) {
        if (this.col != a.row) {
            return null;
        }

        Matrix result = new Matrix(this.row, a.col);

        for (int i = 0; i < this.row; i += blockSize) {
            for (int j = 0; j < a.col; j += blockSize) {
                for (int k = 0; k < this.col; k += blockSize) {
                    // Process block (tile)
                    for (int ii = i; ii < Math.min(i + blockSize, this.row); ii++) {
                        for (int jj = j; jj < Math.min(j + blockSize, a.col); jj++) {
                            double sum = 0.0;
                            for (int kk = k; kk < Math.min(k + blockSize, this.col); kk++) {
                                sum += this.matrix[ii][kk] * a.matrix[kk][jj]; // Multiply and accumulate
                            }
                            result.matrix[ii][jj] += sum; // Accumulate in result matrix
                        }
                    }
                }
            }

            // Update the progress bar
            int progress = (int) (((double) (i + 1) / this.row) * 100);
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(progress);
                progressBar.setString("Progress: " + progress + "%");
            });
        }

        return result;
    }
}
