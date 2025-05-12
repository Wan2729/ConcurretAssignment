package Version1;
import Configuration.*;
import Configuration.Timer;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Sequential approach
        Matrix result;
        Timer timer = new Timer();

        runTest(500, timer);
        runTest(1000, timer);
        runTest(5000, timer);
    }

    public static void runTest(int size, Timer timer) {
        JFrame frame = new JFrame("Matrix Multiplication Progress - " + size + "x" + size);
        frame.setSize(400, 150);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        Matrix A = new Matrix(size, size);
        Matrix B = new Matrix(size, size);
        A.assignRandom();
        B.assignRandom();

        // Retrieve progress bar from Matrix class
        JProgressBar progressBar = A.getProgressBar();
        progressBar.setStringPainted(true);
        frame.add(progressBar, BorderLayout.CENTER);

        frame.setVisible(true);

        // Perform multiplication & track progress
        timer.start();
        Matrix result = A.multiplication(B);
        long elapsedTime = timer.end();

        System.out.println(size + "x" + size + " Time taken with Sequential: " + elapsedTime + " ms");

        // Close frame once test completes
        SwingUtilities.invokeLater(() -> frame.dispose());
    }
}
