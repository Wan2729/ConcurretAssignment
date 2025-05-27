package LAB1;

public class PrintNumLock implements Runnable {

    private int maxNum;
    private Printer print;
    
    public PrintNumLock(Printer print,int maxNum){
        this.print = print;
        this.maxNum = maxNum;
    }

    @Override
    public void run() {
        
        for(int i=0;i<maxNum;i++){
         if(i <= print.maxCount){
             print.printNum(i);
         }
         else{
             System.out.println(i);
         }
        }
    }

}
