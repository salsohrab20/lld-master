package org.practice.atm;

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

    public Account(Builder  builder) {
        this.accountNumber = builder.accountNumber;
        this.accountBalance = builder.accountBalance;
        this.accountHolderName = builder.accountHolderName;
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

    public static class Builder {
        private int accountNumber;
        private int accountBalance;
        private String accountHolderName;

        public Builder accountNumber(int accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public Builder accountBalance(int accountBalance) {
            this.accountBalance = accountBalance;
            return this;
        }

        public Builder accountHolderName(String accountHolderName) {
            this.accountHolderName = accountHolderName;
            return this;
        }

        public Account build() {
            return new Account(this);
        }
    }
}
