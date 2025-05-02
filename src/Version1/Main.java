package Version1;
import Configuration.*;

public class Main {
    public static void main(String[] args) {
        // Sequential approach
        Matrix result;
        Timer timer = new Timer();
        long elapsedTime;

        // 500x500 start
        Matrix a1 = new Matrix(500,500);
        Matrix b1 = new Matrix(500,500);
        a1.assignRandom();
        b1.assignRandom();

        timer.start();
        result = a1.multiplication(b1);
        elapsedTime = timer.end();
        System.out.println("Time taken is "+elapsedTime);
        // 500x500 end

        // 1000x1000 start
        Matrix a2 = new Matrix(1000,1000);
        Matrix b2 = new Matrix(1000,1000);
        a2.assignRandom();
        b2.assignRandom();

        timer.start();
        result = a2.multiplication(b2);
        elapsedTime = timer.end();
        System.out.println("Time taken is "+elapsedTime);
        // 1000x1000 end

        // 5000x5000 start
        Matrix a3 = new Matrix(5000,5000);
        Matrix b3 = new Matrix(5000,5000);
        a3.assignRandom();
        b3.assignRandom();

        timer.start();
        result = a3.multiplication(b3);
        elapsedTime = timer.end();
        System.out.println("Time taken is "+elapsedTime);
        // 5000x5000 end
    }
}
