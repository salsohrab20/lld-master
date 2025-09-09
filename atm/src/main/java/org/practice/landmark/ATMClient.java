package org.practice.landmark;

public class ATMClient {
    public static void main(String[] args) {
        BankService bankService = new BankingServiceImpl();
        CashDispenser cashDispenser = new CashDispenser(10000);
        ATM atmClient = ATM.getInstance(bankService,cashDispenser);

        Card card1 = new Card(123490, 1234,123);
        atmClient.start(card1, 123);
    }
}
