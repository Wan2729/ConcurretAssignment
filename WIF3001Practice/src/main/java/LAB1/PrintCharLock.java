package LAB1;

public class PrintCharLock implements Runnable {

    private char c;
    private int count;
    private Printer print;
    public PrintCharLock(Printer print,char c, int count) {
        this.print = print;
        this.c = c;
        this.count = count;
    }

    @Override
    public void run() {
        for(int i=0;i<count;i++){
            print.printChar(c);
        }
    }

}
