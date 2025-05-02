package Configuration;

import java.util.Random;

public class Matrix {
    int row, col;
    double[][] matrix;
    boolean empty = true;
    boolean init = false;

    public Matrix(int row, int column){
        this.row = row;
        this.col = column;
        matrix = new double[row][column];
        init = true;
    }

    public void assignRandom(){
        if(!init){return;}
        Random random = new Random();
        for(int i=0; i<row; ++i ){
            for(int j=0; j<col; ++j ){
                matrix[i][j] = random.nextDouble(1000);
            }
        }
        empty = false;
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
}
