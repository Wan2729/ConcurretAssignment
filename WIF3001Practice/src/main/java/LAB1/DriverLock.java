package LAB1;

public class DriverLock {

    public static void main(String[] args) {

        Printer print = new Printer(10);
        PrintCharLock test1 = new PrintCharLock(print, 'A', 10);
        PrintNumLock test2 = new PrintNumLock(print,  15);

        Thread thread1 = new Thread(test1);
        Thread thread2 = new Thread(test2);

        thread1.start();
        thread2.start();

    }

}
