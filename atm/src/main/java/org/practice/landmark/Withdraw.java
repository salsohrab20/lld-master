package org.practice.landmark;

public class Withdraw extends Transaction {
    private int amount;
    public Withdraw(Account account, CashDispenser cashDispenser, BankService bankService, int amount) {
        super(account, cashDispenser, bankService);
        this.amount= amount;
    }

    @Override
    public void execute() {
        synchronized (account) {
            if(account.withdraw(amount) && cashDispenser.dispenseCash(amount)) {
                System.out.println("Account withdraw, Account Balance is now " + account.getAccountBalance());
            }
            else{
                System.out.println("Withdraw failed, Insufficient balance in account or in CashDispenser");
            }
        }
    }
}
