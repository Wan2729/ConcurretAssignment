package LAB3;

public class BankAccount {

    private int balance;
    
    public BankAccount(int balance){
        this.balance = balance;
    }

    public synchronized void depositAccount(int amount) {
        balance += amount;
        System.out.println(Thread.currentThread().getName() + " deposit RM" + amount + " | Balance RM" + balance);
    }

    public synchronized void withdrawAccount(int amount) {

        if (amount <= balance) {
            balance -= amount;
            System.out.println(Thread.currentThread().getName() + " withdraw RM" + amount + " | Balance RM" + balance);
        } else {
            System.out.println(Thread.currentThread().getName() + " FAILED to withdraw RM" + amount + " | Balance RM" + balance);
        }
    }

    public int getBalance() {
        return balance;
    }
   

}
