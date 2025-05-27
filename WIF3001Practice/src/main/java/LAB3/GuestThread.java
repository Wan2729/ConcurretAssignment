
package LAB3;

import java.util.Random;

public class GuestThread implements Runnable {

    Room room;
    int guestId;

    public GuestThread(Room room, int guestId) {
        this.room = room;
        this.guestId = guestId;
    }

    @Override
    public void run() {
        Random random = new Random();
        while (true) {
            try {
                room.guestEnterRoom(guestId);
                System.out.println("Guest " + guestId + " staying in the room");
                Thread.sleep(random.nextInt(3000) + 1000);
                room.guestExitRoom(guestId);
                Thread.sleep(random.nextInt(4000) + 1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }
    }

}
