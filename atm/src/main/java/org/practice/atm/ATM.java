package org.practice.atm;

import java.util.Scanner;

public class ATM {
    private static ATM instance ;
    private BankService bankService;
    private CashDispenser cashDispenser;

    private ATM(BankService bankService, CashDispenser cashDispenser) {
        this.bankService = bankService;
        this.cashDispenser = cashDispenser;
    }

    public static ATM getInstance(BankService bankService, CashDispenser cashDispenser) {
        if(instance == null){
            instance = new ATM(bankService, cashDispenser);
        }
        return instance;
    }

    public void start(Card card, int pin){
        if(!bankService.authenticate(card, pin)){
            System.out.println("Account not authenticated");
            return;
        }

        Account account = bankService.getAccount(card.getAccountNumber());
        Scanner sc = new Scanner(System.in);
        while(true){
            System.out.println("\nChoose: 1. Balance 2.Deposit 3.Withdraw 4.Exit");
            int choice = sc.nextInt();

            if(choice == 4) break;

            Transaction txn = null;
            switch (choice) {
                case 1:
                    txn = new BalanceInquiry(account, cashDispenser, bankService);
                    break;
                case 2:
                    System.out.println("Enter amount to Deposit");
                    int depAmount = sc.nextInt();
                    txn = new Deposit(account, cashDispenser, bankService, depAmount);
                    break;
                case 3:
                    System.out.println("Enter amount to Withdraw");
                    int withAmount = sc.nextInt();
                    txn = new Withdraw(account, cashDispenser, bankService, withAmount);
                    break;
            }
            if(txn != null){
                txn.execute();
            }
        }
        sc.close();
    }

}
