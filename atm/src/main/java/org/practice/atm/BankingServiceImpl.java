package org.practice.atm;

import java.util.HashMap;
import java.util.Map;

public class BankingServiceImpl implements BankService {

    private Map<Integer, Account> accountMap = new HashMap();
    private Map<Integer, Card> cardMap = new HashMap<>();

    public BankingServiceImpl() {
       Account account1 = new Account.Builder().accountHolderName("Salman").accountNumber(1234).accountBalance(10000).build();
       Account account2 = new Account.Builder().accountHolderName("Sohrab").accountNumber(1235).accountBalance(1000).build();

       accountMap.put(1234, account1);
       accountMap.put(1235, account2);

       Card card1 = new Card.Builder().cardNumber(123490).accountNumber(1234).pin(123).build();
       Card card2 = new Card.Builder().cardNumber(123491).accountNumber(1235).pin(124).build();

       cardMap.put(1234, card1);
       cardMap.put(1235, card2);
    }

    @Override
    public boolean authenticate(Card card, int pin) {
        return cardMap.containsKey(card.getAccountNumber()) && card.getPin() == pin;
    }

    @Override
    public Account getAccount(int accountNumber) {
       return accountMap.get(accountNumber);
    }
}
