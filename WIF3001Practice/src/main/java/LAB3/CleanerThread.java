/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package LAB3;

import java.util.Random;

/**
 *
 * @author razin
 */
public class CleanerThread implements Runnable {

    Room room;
    int cleanerId;

    public CleanerThread(Room room, int cleanerId) {
        this.room = room;
        this.cleanerId = cleanerId;
    }

    @Override
    public void run() {
        Random random = new Random();
        while (true) {
            try {
                room.cleanerEnterRoom(cleanerId);
                System.out.println("Cleaner " + cleanerId + " cleaning the room");
                Thread.sleep(random.nextInt(4000) + 1000);
                room.cleanerExitRoom(cleanerId);
                Thread.sleep(random.nextInt(5000) + 1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }
    }
}
