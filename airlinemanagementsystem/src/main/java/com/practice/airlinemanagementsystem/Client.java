package com.practice.airlinemanagementsystem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class Client {
    public static void main(String[] args) {
        // Repos
        InMemoryRepository<Flight, String> flightRepo = new InMemoryRepository<>(Flight::getFlightNumber);
        InMemoryRepository<Passenger, String> passengerRepo = new InMemoryRepository<>(Passenger::id);
        InMemoryRepository<Booking, String> bookingRepo = new InMemoryRepository<>(Booking::getId);

        // Create aircraft and flight
        var aircraft = AirCraftFactory.createA320();
        Flight flight = new Flight("AI-101", "DEL", "BOM",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        flight.initializeSeatsFromAircraft(aircraft);
        flightRepo.save(flight);

        // Create passenger
        Passenger p = new Passenger("P1", "Alice", "alice@example.com");
        passengerRepo.save(p);

        BookingService bookingService = new BookingService(flightRepo, passengerRepo, bookingRepo, new ConsoleNotificationService());

        Booking b = bookingService.book(
                p.id(),
                flight.getFlightNumber(),
                SeatClass.ECONOMY,
                BigDecimal.valueOf(5000),
                PaymentMethod.CREDIT_CARD,
                Map.of("cardToken", "tok_visa")
        );

        System.out.println("Booking status: " + b.getBookingStatus() + ", seat: " + b.getSeatNumber());

    }
}
