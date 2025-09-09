package com.practice.airlinemanagementsystem;

import java.math.BigDecimal;
import java.util.Map;

public class PaymentRequest {
    private final BigDecimal amount;
    private final String currency;
    private final Map<String, String> metadata;

    public PaymentRequest(BigDecimal amount, String currency, Map<String, String> metadata) {
        this.amount = amount;
        this.currency = currency;
        this.metadata = metadata;
    }

    public String getCurrency() {
        return currency;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
