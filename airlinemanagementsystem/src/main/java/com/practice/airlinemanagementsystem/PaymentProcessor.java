package com.practice.airlinemanagementsystem;

public interface PaymentProcessor {
    PaymentResult processPayment(PaymentRequest paymentRequest);
}
