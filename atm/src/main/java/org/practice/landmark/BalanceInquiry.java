package org.practice.landmark;

public class BalanceInquiry extends Transaction {

    public BalanceInquiry(Account account, CashDispenser cashDispenser, BankService bankService) {
        super(account, cashDispenser, bankService);
    }

    @Override
    public void execute() {
        System.out.println("Account Balance:" + account.getAccountBalance());
    }
}
