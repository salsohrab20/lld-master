package com.practice.airlinemanagementsystem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Booking {
    private final String id;
    private final String flightNumber;
    private final String passengerId;
    private String paymentTransactionId;
    private String seatNumber;
    private BookingStatus bookingStatus;
    private BigDecimal amount;
    private final Instant createdAt;

    public Booking(Builder builder) {
        this.id = builder.id;
        this.flightNumber = builder.flightNumber;
        this.passengerId = builder.passengerId;
        this.paymentTransactionId = builder.paymentTransactionId;
        this.seatNumber = builder.seatNumber;
        this.bookingStatus = builder.bookingStatus;
        this.amount = builder.amount;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }



    public String getFlightNumber() {
        return flightNumber;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }


    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }



    public static class Builder {
        private String id = UUID.randomUUID().toString();
        private String flightNumber;
        private String passengerId;
        private String paymentTransactionId;
        private String seatNumber;
        private BookingStatus bookingStatus = BookingStatus.PENDING;
        private BigDecimal amount;
        private Instant createdAt = Instant.now();

        public Builder flight(String flightNumber){ this.flightNumber = flightNumber; return this;}
        public Builder passengerId(String passengerId){ this.passengerId = passengerId; return this;}
        public Builder seatNumber(String seatNumber){ this.seatNumber = seatNumber; return this;}
        public Builder amount(BigDecimal amount){ this.amount = amount; return this;}
        public Builder bookingStatus(BookingStatus bookingStatus){ this.bookingStatus = bookingStatus; return this;}
        public Booking build(){ return new Booking(this);}
    }
}
