/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package LAB1;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Printer {

     int maxCount;
    int count = 0;
    private boolean charTurn = true;

    public Printer(int maxCount) {
        this.maxCount = maxCount;
    }

    public synchronized void printChar(char c) {
        while (count >= maxCount) {
            return;
        }
            while (!charTurn) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println(c);
            charTurn = false;
            notifyAll();


    }

    public synchronized void printNum(int num) {
        if (count < maxCount) {
            while (charTurn) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    

                }
            }
            System.out.println(num);
            charTurn = true;
            count++;
            notifyAll();

        }else{
            System.out.println(num);
        }
    }

}
