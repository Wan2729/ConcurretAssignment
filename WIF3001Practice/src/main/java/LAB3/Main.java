package LAB3;

public class Main {

    public static void main(String[] args) {
        Room room = new Room();

        // Start cleaner threads
        for (int i = 0; i < 2; i++) {
            CleanerThread cleaner = new CleanerThread(room, i);
            new Thread(cleaner).start();
        }

        // Start guest threads (up to 6 for MAX_GUEST)
        for (int i = 0; i < 6; i++) {
            GuestThread guest = new GuestThread(room, i);
            new Thread(guest).start();
        }
    }
}
