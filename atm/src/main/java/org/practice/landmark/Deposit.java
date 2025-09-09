package org.practice.landmark;

public class Deposit extends  Transaction{
    int amount;
    public Deposit(Account account, CashDispenser cashDispenser, BankService bankService, int amount) {
        super(account, cashDispenser, bankService);
        this.amount = amount;
    }

    @Override
    public void execute() {
        synchronized (account) {
            account.deposit(amount);
            cashDispenser.deposit(amount);
            System.out.println("Account deposit, Account Balance is now " + account.getAccountBalance());
        }
    }
}
