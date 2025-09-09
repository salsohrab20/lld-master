package com.practice.airlinemanagementsystem;

import java.util.UUID;

public class CreditCardProcessor implements PaymentProcessor {

    @Override
    public PaymentResult processPayment(PaymentRequest paymentRequest) {
        String tx = "cc-" + UUID.randomUUID();
        return new PaymentResult(true, tx,"Charged " + paymentRequest.getAmount());
    }
}
