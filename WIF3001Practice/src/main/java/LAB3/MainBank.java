package LAB3;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainBank {

    public static void main(String[] args) {
        BankAccount bank = new BankAccount(1000);
        Random rand = new Random();

        // Create a thread pool with 10 worker threads
        ExecutorService executor = Executors.newFixedThreadPool(3);

        for (int i = 0; i < 100; i++) {
            final int amount = rand.nextInt(300) + 1;
            final boolean isDeposit = rand.nextBoolean();
            final int index = i;

            Runnable task = (() -> {
                String threadName = "Transaction "+index;
                Thread.currentThread().setName(threadName);
                

                if (isDeposit) {
                    bank.depositAccount(amount);
                } else {
                    bank.depositAccount(amount);
                }

            });
            executor.submit(task);
        }

        // Shut down the executor after all tasks are submitted
        executor.shutdown();

        // Wait until all tasks are finished
        while (!executor.isTerminated()) {
            // Just waiting
        }

        System.out.println("Final Balance: RM" + bank.getBalance());
    }
}
