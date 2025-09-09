package org.practice.landmark;

public interface BankService {
     boolean authenticate(Card card, int pin);
     Account getAccount(int accountNumber);
}
