package LAB2;

import java.util.Random;

public class LargestNumber {

    private int max;
    private int[] arr;

    public LargestNumber(int max) {
        this.max = max;
        arr = new int[max]; // Initialize the array
        createArray();
    }

    public void createArray() {
        Random rand = new Random();
        for (int i = 0; i < max; i++) {
            arr[i] = rand.nextInt();
        }
    }

    public int largestNum() {
        int temp = arr[0]; 
        for (int i = 1; i < arr.length; i++) { 
            if (temp < arr[i]) { 
                temp = arr[i];
            }
        }
        return temp;
    }

    public static void main(String[] args) {
        LargestNumber test1 = new LargestNumber(1000000);
        System.out.println(test1.largestNum());
    }
}
