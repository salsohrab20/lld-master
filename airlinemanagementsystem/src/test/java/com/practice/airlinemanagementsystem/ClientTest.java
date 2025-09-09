package com.practice.airlinemanagementsystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClientTest {

    Repository<Flight,String> flightRepo;
    Repository<Passenger,String> passengerRepo;
    Repository<Booking,String> bookingRepo;
    NotificationService notifier;
    BookingService bookingService;
    PaymentProcessor mockProcessor;

    @BeforeEach
    void setup(){
        flightRepo = mock(Repository.class);
        passengerRepo = mock(Repository.class);
        bookingRepo = mock(Repository.class);
        notifier = mock(NotificationService.class);
        bookingService = new BookingService(flightRepo, passengerRepo, bookingRepo, notifier);

        // register a mock processor
        mockProcessor = mock(PaymentProcessor.class);
        //bookingService.registerProcessor("mockpay", mockProcessor);
    }

    @Test
    void testSuccessfulBooking() {
        Passenger p = new Passenger("P1","Bob","b@e.com");
        Flight f = new Flight("F1","A","B", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        // small aircraft layout
        var a = AirCraftFactory.createA320();
        f.initializeSeatsFromAircraft(a);

        when(passengerRepo.findbyId("P1")).thenReturn(Optional.of(p));
        when(flightRepo.findbyId("F1")).thenReturn(Optional.of(f));
        when(bookingRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(mockProcessor.processPayment(any())).thenReturn(new PaymentResult(true, "tx-123", "ok"));

        Booking b = bookingService.book("P1","F1", SeatClass.ECONOMY,
                BigDecimal.valueOf(1000), PaymentMethod.CREDIT_CARD, Map.of());

        assertNotNull(b);
        assertEquals(BookingStatus.CONFIRMED, b.getBookingStatus());
        assertNotNull(b.getSeatNumber());
        verify(notifier).notifyBooking(eq(b), contains("CONFIRMED"));
    }

    @Test
    void testPaymentFailureRollsBackSeat() {
        Passenger p = new Passenger("P2","Jane","j@e.com");
        Flight f = new Flight("F2","A","B", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        var a = AirCraftFactory.createA320();
        f.initializeSeatsFromAircraft(a);

        when(passengerRepo.findbyId("P2")).thenReturn(Optional.of(p));
        when(flightRepo.findbyId("F2")).thenReturn(Optional.of(f));
        when(bookingRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(mockProcessor.processPayment(any())).thenReturn(new PaymentResult(false, null, "declined"));

        Booking b = bookingService.book("P2","F2", SeatClass.ECONOMY,
                BigDecimal.valueOf(1000), PaymentMethod.CREDIT_CARD, Map.of());

        assertNotNull(b);
        //assertEquals(BookingStatus.FAILED, b.getBookingStatus());
        // seat should have been released -> find any available seat still present
        assertTrue(f.findAvailableSeat(SeatClass.ECONOMY).isPresent());
        verify(notifier).notifyBooking(eq(b), contains("Payment failed"));
    }

}