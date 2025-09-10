package org.practice.atm;

public class CashDispenser {
    private int cashAvailable;

    public CashDispenser(int cashAvailable) {
        this.cashAvailable = cashAvailable;
    }

    public synchronized boolean dispenseCash(int amount) {
        if(this.cashAvailable < amount) {
            return false;
        }
        this.cashAvailable -= amount;
        return true;
    }

    public synchronized boolean deposit(int amount) {
        this.cashAvailable += amount;
        return true;
    }
}
