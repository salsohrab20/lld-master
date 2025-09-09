package com.practice.airlinemanagementsystem;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class BookingService {
    private final Repository<Flight, String> flightRepository;
    private final Repository<Passenger, String> passengerRepository;
    private final Repository<Booking, String> bookingRepository;
    private final Map<PaymentMethod, PaymentProcessor> processors = new HashMap<>();
    private final NotificationService notificationService;

    public BookingService(Repository<Flight, String> flightRepository, Repository<Passenger, String> passengerRepository, Repository<Booking, String> bookingRepository, NotificationService notificationService) {
        this.flightRepository = flightRepository;
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;

        //Register default processors(Strategy)
        processors.put(PaymentMethod.CREDIT_CARD, new CreditCardProcessor());
        processors.put(PaymentMethod.PAYPAL, new CreditCardProcessor());
    }

    public void registerPaymentProcessor(PaymentMethod key,PaymentProcessor processor) {
        processors.put(PaymentMethod.valueOf(key.toString().toLowerCase(Locale.ROOT)), processor);
    }

    public Booking book(String passengerId,String flightNumber, SeatClass seatClass,
                        BigDecimal amount, PaymentMethod paymentMethod, Map<String,String> paymentMetadata) {

        Passenger passenger = passengerRepository.findbyId(passengerId).orElseThrow(() -> new IllegalArgumentException("passenger not found"));
        Flight flight = flightRepository.findbyId(flightNumber).orElseThrow(() -> new IllegalArgumentException("flight not found"));
        flight.getLock().lock();
        
        Seat reservedSeat = null;
        try{
            Optional<Seat> availableSeat = flight.findAvailableSeat(seatClass);
            if(availableSeat.isEmpty()){
                throw new IllegalStateException("seat not found in class : "+ seatClass);
            }
            
            reservedSeat = availableSeat.get();
            boolean reserveOk = reservedSeat.reserve();
            if(!reserveOk){ throw new IllegalStateException("seat not reserved"); }

        }
        finally{
            flight.getLock().unlock();
        }

        Booking booking = new Booking.Builder().flight(flight.getFlightNumber())
                .flight(flight.getFlightNumber())
                .passengerId(passenger.id())
                .seatNumber(reservedSeat.getSeatNumber())
                .amount(amount)
                .build();

        bookingRepository.save(booking);

        PaymentProcessor processor = processors.getOrDefault(paymentMethod.toString().toLowerCase(Locale.ROOT), processors.get(PaymentMethod.CREDIT_CARD));
        PaymentRequest paymentRequest = new PaymentRequest(amount, "USD", paymentMetadata);

        PaymentResult paymentResult = processor.processPayment(paymentRequest);

        if(paymentResult.isSuccess()){
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            booking.setPaymentTransactionId(paymentResult.getTransactionId());
            bookingRepository.save(booking);

            notificationService.notifyBooking(booking, "Your booking is CONFIRMED. Seat: "+ booking.getSeatNumber());
            return booking;
        }
        else
        {
            flight.findSeat(reservedSeat.getSeatNumber()).ifPresent(Seat::release);
            booking.setBookingStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);
            notificationService.notifyBooking(booking, "Payment failed: " + paymentResult.getMessage());
            return booking;
        }
    }

    public Optional<Booking> getBooking(String id) { return bookingRepository.findbyId(id); }
}
