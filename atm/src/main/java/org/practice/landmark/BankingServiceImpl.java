package org.practice.landmark;

import java.util.HashMap;
import java.util.Map;

public class BankingServiceImpl implements BankService {

    private Map<Integer, Account> accountMap = new HashMap();
    private Map<Integer, Card> cardMap = new HashMap<>();

    public BankingServiceImpl() {
       Account account1 = new Account(1234,10000,"Salman");
       Account account2 = new Account(1235,1000,"Sohrab");

       accountMap.put(1234, account1);
       accountMap.put(1235, account2);

       Card card1 = new Card(123490, 1234,123);
       Card card2 = new Card(123491, 1235,124);

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
