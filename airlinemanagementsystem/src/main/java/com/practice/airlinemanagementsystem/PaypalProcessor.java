package com.practice.airlinemanagementsystem;

import java.util.UUID;

public class PaypalProcessor implements PaymentProcessor {

    @Override
    public PaymentResult processPayment(PaymentRequest paymentRequest) {
        String tx = "pp-" + UUID.randomUUID().toString();
        return new PaymentResult(true, tx,"Paypal payment success");
    }
}
