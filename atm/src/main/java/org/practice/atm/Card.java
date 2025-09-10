package org.practice.atm;

public class Card {
    private int cardNumber;
    private int accountNumber;
    private int pin;

    public Card(Builder builder) {
        this.cardNumber = builder.cardNumber;
        this.accountNumber = builder.accountNumber;
        this.pin = builder.pin;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public int getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(int cardNumber) {
        this.cardNumber = cardNumber;
    }

    public static class Builder{
        private int cardNumber;
        private int accountNumber;
        private int pin;

        public Builder cardNumber(int cardNumber) {
            this.cardNumber = cardNumber;
            return this;
        }

        public Builder accountNumber(int accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public Builder pin(int pin) {
            this.pin = pin;
            return this;
        }

        public Card build() {
            return new Card(this);
        }
    }
}
