package LAB2;

public class FindMax implements Runnable {

    private int start;
    private int end;
    private int[] arr;
    private int position;
    private int maxNum = 0;
    private long totalTimes;
    private long startTime;
    private long endTime;

    public FindMax(int start, int end, int[] arr) {
        this.start = start;
        this.end = end;
        this.arr = arr;
        this.position = -1;

    }

    public static void main(String[] args) {
        LargestNumberConcurrent test1 = new LargestNumberConcurrent(1000);
        test1.run();
    }

    @Override
    public void run() {
        startTime = (int) System.currentTimeMillis();
        for (int i = start; i < end; i++) {
            if (arr[i] > maxNum) {
                maxNum = arr[i];
                position = i;
            }
        }
        endTime = (int) System.currentTimeMillis();
    }

    public int getPosition() {
        return position;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public long getTotalTimes() {
        totalTimes = endTime - startTime;
        return totalTimes;
    }

}
