package LAB2;

import java.util.Random;
import java.util.Arrays;

public class LargestNumberConcurrent implements Runnable {

    Thread thread1;
    Thread thread2;
    private int[] arr;
    private int max;

    public LargestNumberConcurrent(int max) {
        this.max = max;
        createArray();

    }

    public void createArray() {
        Random rand = new Random();
        arr = new int[max];
        for (int i = 0; i < max; i++) {
            arr[i] = rand.nextInt();
        }
    }

    public void split(Thread thread, int[] arr, int start, int end) {

    }

    @Override
    public void run() {

        int middle = arr.length / 2;
        int end = arr.length;
        int[] firstHalf = Arrays.copyOfRange(arr, 0, middle);
        int[] secondHalf = Arrays.copyOfRange(arr, middle + 1, end);

        FindMax test1 = new FindMax(0, middle, firstHalf);
        FindMax test2 = new FindMax(middle + 1, end, secondHalf);
        thread1 = new Thread(test1);
        thread2 = new Thread(test2);

        thread1.setName("Thread 1");
        thread2.setName("Thread 2");
        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        int maxNumber;
        int position;
        String getThread = "";
        long times;

        if (test1.getMaxNum() > test2.getMaxNum()) {
            maxNumber = test1.getMaxNum();
            position = test1.getPosition();
            getThread = thread1.getName();
            times = test1.getTotalTimes();
        } else {
            maxNumber = test2.getMaxNum();
            position = test2.getPosition();
            getThread = thread2.getName();
            times = test2.getTotalTimes();
        }

        System.out.println("Max number is " + maxNumber);
        System.out.println("in Position " + position);
        System.out.println("Find in thread " + getThread);
        System.out.println("Total times: "+times);
    }

}
