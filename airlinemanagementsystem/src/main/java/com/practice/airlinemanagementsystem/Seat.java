package com.practice.airlinemanagementsystem;

import java.util.concurrent.atomic.AtomicBoolean;

public class Seat {
    private final String seatNumber;
    private final SeatClass seatClass;
    private final AtomicBoolean seatAvailability = new AtomicBoolean(true);

    public Seat(String seatNumber, SeatClass seatClass) {
        this.seatNumber = seatNumber;
        this.seatClass = seatClass;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public boolean isAvailable(){
        return seatAvailability.get();
    }

    public boolean reserve(){
        return seatAvailability.compareAndSet(true,false);
    }

    public void release(){
        seatAvailability.set(true);
    }
}
