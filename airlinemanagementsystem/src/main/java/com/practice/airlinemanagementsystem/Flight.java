package com.practice.airlinemanagementsystem;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class Flight {
    private final String flightNumber;
    private final String source;
    private final String destination;
    private final LocalDateTime departureTime;
    private final LocalDateTime arrivalTime;
    private final Map<String,Seat> seats = new LinkedHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public Flight(String flightNumber, String source, String destination, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.flightNumber = flightNumber;
        this.source = source;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public Map<String, Seat> getSeats() {
        return seats;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public String getDestination() {
        return destination;
    }

    public String getSource() {
        return source;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public void initializeSeatsFromAircraft(Aircraft aircraft) {
        aircraft.getSeatTemplate().forEach((seatNumber, seatClass) -> {
            seats.put(seatNumber,new Seat(seatNumber,seatClass));
        });
    }

    public Optional<Seat> findAvailableSeat(SeatClass seatClass) {
       return seats.values().stream().filter(seat-> seat.getSeatClass().equals(seatClass) &&
                seat.isAvailable()).findFirst();

    }

    public Optional<Seat> findSeat(String seatNumber) {
        return Optional.ofNullable(seats.get(seatNumber));
    }

}
