package org.practice.atm;

public abstract class Transaction {
    protected Account account;
    protected CashDispenser cashDispenser;
    protected BankService bankService;

    public Transaction(Account account, CashDispenser cashDispenser, BankService bankService) {
        this.account = account;
        this.cashDispenser = cashDispenser;
        this.bankService = bankService;
    }

    public abstract void execute();
}
