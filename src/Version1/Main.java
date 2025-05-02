package Version1;
import Configuration.*;

public class Main {
    public static void main(String[] args) {
        // Sequential approach

        // 500x500 start
        Matrix a = new Matrix(3,3);
        Matrix b = new Matrix(3,3);
        Matrix result;
        a.assignRandom();
        b.assignRandom();
        System.out.println(a);
        System.out.println(b);

        Timer timer = new Timer();
        timer.start();
        result = a.multiplication(b);
        long elapsedTime = timer.end();
        System.out.println("Matrix Result\n"+result);
        System.out.println("Time taken is "+elapsedTime);
        // 500x500 end

    }
}
