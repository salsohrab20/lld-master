package org.practice.atm;

public class ATMClient {
    public static void main(String[] args) {
        BankService bankService = new BankingServiceImpl();
        CashDispenser cashDispenser = new CashDispenser(10000);
        ATM atmClient = ATM.getInstance(bankService,cashDispenser);

        Card card1 = new Card.Builder().cardNumber(123490).accountNumber(1234).pin(123).build();
        atmClient.start(card1, 123);
    }
}
