package LAB3;

public class Room {

    private final int MAX_GUEST = 6;
    private boolean cleanerInRoom = false;
    private int numberOfGuest = 0;

    public synchronized void cleanerEnterRoom(int cleanerId) {
        while (numberOfGuest > 0 || cleanerInRoom) {
            try {
                System.out.println("Cleaner " + cleanerId + " is waiting to enter the room");
                wait();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Cleaner " + cleanerId + " entered to enter the room");
        cleanerInRoom = true;

    }

    public synchronized void cleanerExitRoom(int cleanerId) {
        cleanerInRoom = false;
        System.out.println("Cleaner " + cleanerId + " exit the room");
        notifyAll();

    }

    public synchronized void guestEnterRoom(int guestId) {
        try {
            while (cleanerInRoom || numberOfGuest >= MAX_GUEST) {
                System.out.println("Guest " + guestId + " is waiting to enter the room");
                wait();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        numberOfGuest++;
        System.out.println("Guest " + guestId + "  entered the room");
        notifyAll();

    }

    public synchronized void guestExitRoom(int guestId) {
        numberOfGuest--;
        System.out.println("Guest " + guestId + "  exit the room");
        notifyAll();
    }

}
