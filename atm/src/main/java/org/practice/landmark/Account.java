package org.practice.landmark;

public class Account {
    private int accountNumber;
    private int accountBalance;
    private String accountHolderName;

    public int getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(int accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public Account(int accountNumber, int accountBalance, String accountHolderName) {
        this.accountNumber = accountNumber;
        this.accountBalance = accountBalance;
        this.accountHolderName = accountHolderName;
    }

    public synchronized boolean withdraw(int amount) {
        if(this.accountBalance < amount) {
            return false;
        }
        this.accountBalance -= amount;
        return true;
    }

    public synchronized boolean deposit(int amount) {
        this.accountBalance += amount;
        return true;
    }
}
