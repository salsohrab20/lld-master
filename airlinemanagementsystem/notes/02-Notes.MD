# Problem Statement
- Design and implement an Airline Management System that allows users to book flights, manage passengers, handle seat assignments, process payments, and track bookings and flights.

# Requirements
```text
Flight Management: The system manages flights, each with a unique flight number, aircraft, source, destination, and schedule.
Aircraft Management: Each flight is associated with an aircraft, which has a model and a set of seats.
Seat Management: The system manages seat assignments and availability for each flight.
Passenger Management: Passengers can be added, updated, and associated with bookings.
Booking Management: Users can book flights, and the system tracks bookings, assigned seats, and passengers.
Payment Processing: The system processes payments for bookings.
Extensibility: Easy to add new features such as loyalty programs, meal selection, or multi-leg journeys.
```
---

- Flight, Aircraft, source,destination,seat(SeatClass),passengers,users booking (Booking class), Payment(PaymentResult)

# 1) Step-by-step approach to design

1. **Clarify requirements & use cases**

    * Book flight, assign seat, process payment, view/cancel booking, manage passengers, list flights.
    * Nonfunctional: concurrency when many users book same flight; extensibility.

2. **Model the domain**

    * Entities: Aircraft, Flight, Seat, Passenger, Booking, Payment.
    * Value objects/enums: SeatClass, BookingStatus, PaymentResult.

3. **Define boundaries / layers**

    * Domain (models), Repositories (persistence interface), Services (business logic), Adapters (external payment gateways), API layer (not implemented here; easy to add later).

4. **Pick design patterns** (mapped to functionality)

    * **Repository** (abstract persistence) — for Flight/Booking/Passenger.
    * **Factory / Builder** — create Aircraft templates and Booking objects.
    * **Strategy** — payment processors (CreditCard, PayPal...).
    * **Adapter** — adapt third-party payment SDKs into our PaymentProcessor.
    * **Observer / Event** — notifications, extension hooks like loyalty program.
    * **Singleton (optional)** — registry / factories when global.
    * **State** — booking life cycle (PENDING → CONFIRMED → CANCELLED).
    * **Flyweight** (conceptual) — share seat templates in Aircraft; flight copies availability.

5. **Concurrency / Transactions**

    * Seat allocation must be atomic — use a lock per Flight or synchronized block to avoid double assignment.
    * Two-phase approach: reserve seat locally, process payment, commit or rollback.

6. **Implement & test**

    * Implement core with in-memory repositories and thorough unit tests (JUnit + Mockito).
    * Add integration later (Spring + RDBMS) by replacing repositories.

---

# 2) Architecture & pattern mapping (short)

* **BookingService** — core orchestrator (Template/Facade).
* **PaymentProcessor** (Strategy) & **PaymentAdapter** (Adapter).
* **AircraftFactory** (Factory) to produce aircraft seat maps.
* **Repositories** (Repository pattern) with in-memory implementations for demo.
* **SeatAllocator** (part of BookingService) with synchronized reservation.
* **NotificationService** (Observer) to receive booking events.
* **BookingBuilder** (Builder) for clean booking creation.

---

# 3) Code — core Java implementation (runnable)

```java
// File: com/example/airline/model/SeatClass.java
package com.example.airline.model;
public enum SeatClass { ECONOMY, BUSINESS, FIRST }
```

```java
// File: com/example/airline/model/BookingStatus.java
package com.example.airline.model;
public enum BookingStatus { PENDING, CONFIRMED, FAILED, CANCELLED }
```

```java
// File: com/example/airline/model/Seat.java
package com.example.airline.model;

import java.util.concurrent.atomic.AtomicBoolean;

public class Seat {
    private final String seatNumber;
    private final SeatClass seatClass;
    private final AtomicBoolean available = new AtomicBoolean(true);

    public Seat(String seatNumber, SeatClass seatClass) {
        this.seatNumber = seatNumber;
        this.seatClass = seatClass;
    }

    public String getSeatNumber() { return seatNumber; }
    public SeatClass getSeatClass() { return seatClass; }

    public boolean isAvailable() { return available.get(); }
    public boolean reserve() { return available.compareAndSet(true, false); }
    public void release() { available.set(true); }
}
```

```java
// File: com/example/airline/model/Aircraft.java
package com.example.airline.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aircraft holds a seat template (seat numbers & classes).
 * Flight will copy seats for flight-specific availability.
 */
public class Aircraft {
    private final String model;
    private final Map<String, SeatClass> seatTemplate = new LinkedHashMap<>();

    public Aircraft(String model) {
        this.model = model;
    }

    public String getModel() { return model; }

    public void addSeatTemplate(String seatNumber, SeatClass seatClass) {
        seatTemplate.put(seatNumber, seatClass);
    }

    public Map<String, SeatClass> getSeatTemplate() { return seatTemplate; }
}
```

```java
// File: com/example/airline/model/Flight.java
package com.example.airline.model;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Flight has a seat map (seatNumber -> Seat) which is flight-specific.
 */
public class Flight {
    private final String flightNumber;
    private final String source;
    private final String destination;
    private final LocalDateTime departure;
    private final LocalDateTime arrival;
    private final Map<String, Seat> seats = new LinkedHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public Flight(String flightNumber, String source, String destination,
                  LocalDateTime departure, LocalDateTime arrival) {
        this.flightNumber = flightNumber;
        this.source = source;
        this.destination = destination;
        this.departure = departure;
        this.arrival = arrival;
    }

    public String getFlightNumber() { return flightNumber; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public LocalDateTime getDeparture() { return departure; }
    public LocalDateTime getArrival() { return arrival; }

    public Map<String, Seat> getSeats() { return seats; }
    public ReentrantLock getLock() { return lock; }

    public void initializeSeatsFromAircraft(Aircraft aircraft) {
        aircraft.getSeatTemplate().forEach((seatNo, sclass) ->
            seats.put(seatNo, new Seat(seatNo, sclass))
        );
    }

    public Optional<Seat> findAvailableSeat(SeatClass seatClass) {
        return seats.values().stream()
                .filter(s -> s.getSeatClass() == seatClass && s.isAvailable())
                .findFirst();
    }

    public Optional<Seat> findSeat(String seatNumber) {
        return Optional.ofNullable(seats.get(seatNumber));
    }
}
```

```java
// File: com/example/airline/model/Passenger.java
package com.example.airline.model;
import java.util.Objects;
public class Passenger {
    private final String id;
    private String name;
    private String email;
    public Passenger(String id, String name, String email) {
        this.id = id; this.name = name; this.email = email;
    }
    public String getId(){ return id; }
    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
    public String getEmail(){ return email; }
    public void setEmail(String email){ this.email = email; }

    @Override public boolean equals(Object o){
        if(this==o) return true;
        if(o==null || getClass()!=o.getClass()) return false;
        Passenger p=(Passenger)o; return Objects.equals(id,p.id);
    }
    @Override public int hashCode(){ return Objects.hash(id); }
}
```

```java
// File: com/example/airline/model/Booking.java
package com.example.airline.model;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Booking {
    private final String id;
    private final String flightNumber;
    private final String passengerId;
    private String seatNumber;
    private BookingStatus status;
    private BigDecimal amount;
    private String paymentTransactionId;
    private final Instant createdAt;

    private Booking(Builder b){
        this.id = b.id;
        this.flightNumber = b.flightNumber;
        this.passengerId = b.passengerId;
        this.seatNumber = b.seatNumber;
        this.status = b.status;
        this.amount = b.amount;
        this.paymentTransactionId = b.paymentTransactionId;
        this.createdAt = b.createdAt;
    }

    public String getId(){ return id; }
    public String getFlightNumber(){ return flightNumber; }
    public String getPassengerId(){ return passengerId; }
    public String getSeatNumber(){ return seatNumber; }
    public void setSeatNumber(String s){ this.seatNumber = s; }
    public BookingStatus getStatus(){ return status; }
    public void setStatus(BookingStatus s){ this.status = s; }
    public BigDecimal getAmount(){ return amount; }
    public void setAmount(BigDecimal a){ this.amount = a; }
    public String getPaymentTransactionId(){ return paymentTransactionId; }
    public void setPaymentTransactionId(String id){ this.paymentTransactionId = id; }
    public Instant getCreatedAt(){ return createdAt; }

    public static class Builder {
        private String id = UUID.randomUUID().toString();
        private String flightNumber;
        private String passengerId;
        private String seatNumber;
        private BookingStatus status = BookingStatus.PENDING;
        private BigDecimal amount;
        private String paymentTransactionId;
        private Instant createdAt = Instant.now();

        public Builder flight(String f){ this.flightNumber=f; return this; }
        public Builder passenger(String p){ this.passengerId=p; return this; }
        public Builder seat(String s){ this.seatNumber=s; return this; }
        public Builder status(BookingStatus st){ this.status=st; return this; }
        public Builder amount(BigDecimal a){ this.amount=a; return this; }
        public Booking build(){ return new Booking(this); }
    }
}
```

```java
// File: com/example/airline/payment/PaymentRequest.java
package com.example.airline.payment;
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
    public BigDecimal getAmount(){ return amount; }
    public String getCurrency(){ return currency; }
    public Map<String,String> getMetadata(){ return metadata; }
}
```

```java
// File: com/example/airline/payment/PaymentResult.java
package com.example.airline.payment;
public class PaymentResult {
    private final boolean success;
    private final String transactionId;
    private final String message;
    public PaymentResult(boolean success, String transactionId, String message){
        this.success = success; this.transactionId = transactionId; this.message = message;
    }
    public boolean isSuccess(){ return success; }
    public String getTransactionId(){ return transactionId; }
    public String getMessage(){ return message; }
}
```

```java
// File: com/example/airline/payment/PaymentProcessor.java
package com.example.airline.payment;
public interface PaymentProcessor {
    PaymentResult process(PaymentRequest request);
}
```

```java
// File: com/example/airline/payment/CreditCardProcessor.java
package com.example.airline.payment;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Simple simulated credit card processor (Strategy).
 */
public class CreditCardProcessor implements PaymentProcessor {
    @Override
    public PaymentResult process(PaymentRequest request) {
        // simulate card auth (always succeed in this demo)
        String tx = "cc-" + UUID.randomUUID();
        return new PaymentResult(true, tx, "Charged " + request.getAmount());
    }
}
```

```java
// File: com/example/airline/payment/PayPalProcessor.java
package com.example.airline.payment;
import java.util.UUID;

public class PayPalProcessor implements PaymentProcessor {
    @Override
    public PaymentResult process(PaymentRequest request) {
        // simulate PayPal
        String tx = "pp-" + UUID.randomUUID();
        return new PaymentResult(true, tx, "PayPal payment success");
    }
}
```

```java
// File: com/example/airline/payment/StripeGateway.java
package com.example.airline.payment;
/**
 * Simulated external Stripe SDK client
 */
public class StripeGateway {
    public String charge(String cardToken, String amountCents) {
        // pretend we call stripe and return id
        return "stripe_tx_" + System.nanoTime();
    }
}
```

```java
// File: com/example/airline/payment/StripeAdapter.java
package com.example.airline.payment;
import java.util.Map;

/**
 * Adapter pattern: adapts external StripeGateway to our PaymentProcessor interface.
 */
public class StripeAdapter implements PaymentProcessor {
    private final StripeGateway gateway = new StripeGateway();

    @Override
    public PaymentResult process(PaymentRequest request) {
        // adapt metadata
        Map<String,String> meta = request.getMetadata();
        String token = meta.getOrDefault("cardToken", "tok_test");
        String amountCents = request.getAmount().multiply(java.math.BigDecimal.valueOf(100)).toBigInteger().toString();
        String tx = gateway.charge(token, amountCents);
        return new PaymentResult(true, tx, "Charged via Stripe");
    }
}
```

```java
// File: com/example/airline/repo/Repository.java
package com.example.airline.repo;
import java.util.Optional;
import java.util.List;
public interface Repository<T, ID> {
    T save(T t);
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
}
```

```java
// File: com/example/airline/repo/InMemoryRepository.java
package com.example.airline.repo;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Generic in-memory repository for demo.
 */
public class InMemoryRepository<T, ID> implements Repository<T, ID> {
    protected final Map<ID, T> store = new ConcurrentHashMap<>();
    private final java.util.function.Function<T, ID> idExtractor;

    public InMemoryRepository(java.util.function.Function<T, ID> idExtractor) {
        this.idExtractor = idExtractor;
    }

    @Override
    public T save(T t) {
        ID id = idExtractor.apply(t);
        store.put(id, t);
        return t;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(ID id) { store.remove(id); }
}
```

```java
// File: com/example/airline/service/NotificationService.java
package com.example.airline.service;
import com.example.airline.model.Booking;
public interface NotificationService {
    void notifyBooking(Booking booking, String message);
}
```

```java
// File: com/example/airline/service/ConsoleNotificationService.java
package com.example.airline.service;
import com.example.airline.model.Booking;
public class ConsoleNotificationService implements NotificationService {
    @Override
    public void notifyBooking(Booking booking, String message) {
        System.out.println("[NOTIFY] Booking " + booking.getId() + " -> " + message);
    }
}
```

```java
// File: com/example/airline/service/AircraftFactory.java
package com.example.airline.service;
import com.example.airline.model.Aircraft;
import com.example.airline.model.SeatClass;

/**
 * Factory that creates aircraft seat templates.
 */
public class AircraftFactory {
    public static Aircraft createA320() {
        Aircraft a = new Aircraft("Airbus A320");
        // simple template: 6 rows, seats A-F, row 1-2 -> business, others economy
        for (int row=1; row<=6; row++){
            for (char c='A'; c<='F'; c++){
                String seat = row + String.valueOf(c);
                if (row <= 2) a.addSeatTemplate(seat, SeatClass.BUSINESS);
                else a.addSeatTemplate(seat, SeatClass.ECONOMY);
            }
        }
        return a;
    }
}
```

```java
// File: com/example/airline/service/BookingService.java
package com.example.airline.service;

import com.example.airline.model.*;
import com.example.airline.payment.*;
import com.example.airline.repo.*;

import java.math.BigDecimal;
import java.util.*;

public class BookingService {
    private final Repository<Flight, String> flightRepo;
    private final Repository<Passenger, String> passengerRepo;
    private final Repository<Booking, String> bookingRepo;
    private final Map<String, PaymentProcessor> processors = new HashMap<>();
    private final NotificationService notifier;

    public BookingService(Repository<Flight, String> flightRepo,
                          Repository<Passenger, String> passengerRepo,
                          Repository<Booking, String> bookingRepo,
                          NotificationService notifier) {
        this.flightRepo = flightRepo;
        this.passengerRepo = passengerRepo;
        this.bookingRepo = bookingRepo;
        this.notifier = notifier;

        // Register default processors (Strategy)
        processors.put("creditcard", new CreditCardProcessor());
        processors.put("paypal", new PayPalProcessor());
        processors.put("stripe", new StripeAdapter());
    }

    public void registerProcessor(String key, PaymentProcessor processor){
        processors.put(key.toLowerCase(Locale.ROOT), processor);
    }

    /**
     * Book a flight:
     *   1. check passenger exists
     *   2. lock flight, find seat, reserve seat atomically
     *   3. create PENDING booking and persist
     *   4. process payment
     *   5. on success mark booking CONFIRMED; on failure rollback seat and mark FAILED
     */
    public Booking book(String passengerId, String flightNumber, SeatClass seatClass,
                        BigDecimal amount, String paymentMethod, Map<String,String> paymentMeta) {
        Passenger p = passengerRepo.findById(passengerId).orElseThrow(() -> new IllegalArgumentException("No passenger"));
        Flight f = flightRepo.findById(flightNumber).orElseThrow(() -> new IllegalArgumentException("No flight"));

        // Lock per flight to avoid race conditions when finding/reserving seat
        f.getLock().lock();
        Seat reservedSeat = null;
        try {
            Optional<Seat> maybe = f.findAvailableSeat(seatClass);
            if (maybe.isEmpty()) {
                throw new IllegalStateException("No seats available in class " + seatClass);
            }
            reservedSeat = maybe.get();
            // reserve logically
            boolean ok = reservedSeat.reserve();
            if (!ok) throw new IllegalStateException("Could not reserve seat");
        } finally {
            f.getLock().unlock();
        }

        // Build booking
        Booking booking = new Booking.Builder()
                .flight(f.getFlightNumber())
                .passenger(p.getId())
                .seat(reservedSeat.getSeatNumber())
                .amount(amount)
                .build();
        bookingRepo.save(booking);

        // Process Payment
        PaymentProcessor processor = processors.getOrDefault(paymentMethod.toLowerCase(Locale.ROOT),
                processors.get("creditcard"));

        PaymentRequest req = new PaymentRequest(amount, "USD", paymentMeta);
        PaymentResult res = processor.process(req);

        if (res.isSuccess()) {
            booking.setPaymentTransactionId(res.getTransactionId());
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepo.save(booking);
            notifier.notifyBooking(booking, "Your booking is CONFIRMED. Seat: " + booking.getSeatNumber());
            return booking;
        } else {
            // rollback seat
            f.findSeat(reservedSeat.getSeatNumber()).ifPresent(Seat::release);
            booking.setStatus(BookingStatus.FAILED);
            bookingRepo.save(booking);
            notifier.notifyBooking(booking, "Payment failed: " + res.getMessage());
            return booking;
        }
    }

    // other helpers: cancelBooking, getBooking etc.
    public Optional<Booking> getBooking(String id) { return bookingRepo.findById(id); }
}
```

```java
// File: com/example/airline/AppDemo.java
package com.example.airline;
import com.example.airline.model.*;
import com.example.airline.repo.*;
import com.example.airline.service.*;
import com.example.airline.payment.*;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Map;

public class AppDemo {
    public static void main(String[] args) {
        // Repos
        InMemoryRepository<Flight, String> flightRepo = new InMemoryRepository<>(Flight::getFlightNumber);
        InMemoryRepository<Passenger, String> passengerRepo = new InMemoryRepository<>(Passenger::getId);
        InMemoryRepository<Booking, String> bookingRepo = new InMemoryRepository<>(Booking::getId);

        // Create aircraft and flight
        var aircraft = AircraftFactory.createA320();
        Flight flight = new Flight("AI-101", "DEL", "BOM",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        flight.initializeSeatsFromAircraft(aircraft);
        flightRepo.save(flight);

        // Create passenger
        Passenger p = new Passenger("P1", "Alice", "alice@example.com");
        passengerRepo.save(p);

        BookingService bookingService = new BookingService(flightRepo, passengerRepo, bookingRepo, new ConsoleNotificationService());
        // use stripe adapter to simulate
        bookingService.registerProcessor("mystripe", new StripeAdapter());

        Booking b = bookingService.book(
                p.getId(),
                flight.getFlightNumber(),
                SeatClass.ECONOMY,
                BigDecimal.valueOf(5000),
                "creditcard",
                Map.of("cardToken", "tok_visa")
        );

        System.out.println("Booking status: " + b.getStatus() + ", seat: " + b.getSeatNumber());
    }
}
```

---

# 4) Unit tests (JUnit 5 + Mockito)

Below are representative unit tests for `BookingService`. They mock payment processors and repositories to test booking success/failure.

```xml
<!-- pom.xml dependencies snippet -->
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.9.3</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>5.4.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

```java
// File: com/example/airline/service/BookingServiceTest.java
package com.example.airline.service;

import com.example.airline.model.*;
import com.example.airline.payment.*;
import com.example.airline.repo.*;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

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
        bookingService.registerProcessor("mockpay", mockProcessor);
    }

    @Test
    void testSuccessfulBooking() {
        Passenger p = new Passenger("P1","Bob","b@e.com");
        Flight f = new Flight("F1","A","B", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        // small aircraft layout
        var a = com.example.airline.service.AircraftFactory.createA320();
        f.initializeSeatsFromAircraft(a);

        when(passengerRepo.findById("P1")).thenReturn(Optional.of(p));
        when(flightRepo.findById("F1")).thenReturn(Optional.of(f));
        when(bookingRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(mockProcessor.process(any())).thenReturn(new PaymentResult(true, "tx-123", "ok"));

        Booking b = bookingService.book("P1","F1", SeatClass.ECONOMY,
                BigDecimal.valueOf(1000), "mockpay", Map.of());

        assertNotNull(b);
        assertEquals(BookingStatus.CONFIRMED, b.getStatus());
        assertNotNull(b.getSeatNumber());
        verify(notifier).notifyBooking(eq(b), contains("CONFIRMED"));
    }

    @Test
    void testPaymentFailureRollsBackSeat() {
        Passenger p = new Passenger("P2","Jane","j@e.com");
        Flight f = new Flight("F2","A","B", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        var a = com.example.airline.service.AircraftFactory.createA320();
        f.initializeSeatsFromAircraft(a);

        when(passengerRepo.findById("P2")).thenReturn(Optional.of(p));
        when(flightRepo.findById("F2")).thenReturn(Optional.of(f));
        when(bookingRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(mockProcessor.process(any())).thenReturn(new PaymentResult(false, null, "declined"));

        Booking b = bookingService.book("P2","F2", SeatClass.ECONOMY,
                BigDecimal.valueOf(1000), "mockpay", Map.of());

        assertNotNull(b);
        assertEquals(BookingStatus.FAILED, b.getStatus());
        // seat should have been released -> find any available seat still present
        assertTrue(f.findAvailableSeat(SeatClass.ECONOMY).isPresent());
        verify(notifier).notifyBooking(eq(b), contains("Payment failed"));
    }
}
```

---

# 5) How this satisfies the requirements

* **Flight Management**: `Flight` with number/source/destination/schedule.
* **Aircraft Management**: `Aircraft` with seat template; factory to create aircraft models.
* **Seat Management**: Flight-specific `Seat` instances with atomic reservation.
* **Passenger Management**: `Passenger` model + repository.
* **Booking Management**: `BookingService` orchestrates booking, seat assignment, status changes.
* **Payment Processing**: `PaymentProcessor` strategy and `StripeAdapter` adapter for third-party SDK.
* **Extensibility**: You can add new `PaymentProcessor`, add `NotificationService` implementations, attach observers for loyalty program, add meal selection to `Booking` model, support multi-leg by adding `Itinerary` entity.

---

# 6) Extending the system (quick notes)

* **Loyalty program**: implement `LoyaltyService` (Observer). After `CONFIRMED`, emit BookingConfirmed event — loyalty service awards points. Use event bus or observer list injected into `BookingService`.
* **Multi-leg journeys**: introduce `Itinerary` with list of `FlightLeg` and adapt booking flow to reserve seats across legs (use transactional-like semantic: reserve all seats then pay).
* **Meals / Preferences**: expand `Booking` with `List<MealSelection>`, `SeatPreference`.
* **Persistence / Transactions**: replace `InMemoryRepository` with Spring Data repositories + RDBMS. Wrap booking flow in a DB transaction — seat reservation becomes row update with optimistic/pessimistic locking.
* **REST / Spring Boot**: add `@RestController` endpoints that call `BookingService`. Replace manual locking with DB locks.

---

# 7) Final tips & next steps

* For production, use database transactions to avoid race conditions rather than in-process locks when you have multiple app instances.
* Add idempotency keys for payment calls and retries.
* Add audit logs and metrics to observe booking failures.
* Add unit + integration tests. The tests above show how to structure JUnit + Mockito tests for business flow.

---

# ✈️ Airline Management System – Interview Prep Sheet

---

## 1. What are the core use-cases of the system?

* Manage flights (CRUD operations on flights, aircraft, and schedules).
* Manage passengers (register, update info).
* Book flights (seat allocation, payments, booking tracking).
* Cancel / update bookings.
* Extensibility for loyalty programs, meal selection, multi-leg journeys.

👉 **Answer Tip**: Always start with the *happy path*, then mention edge cases & extensions.

---

## 2. What are the main entities and their relationships?

* **Flight** → has schedule, aircraft, seats.
* **Aircraft** → defines seat templates.
* **Seat** → belongs to flight, has class (economy, business).
* **Passenger** → linked to bookings.
* **Booking** → links passenger + flight + seat + payment.
* **Payment** → transaction details.

👉 **Answer Tip**: Draw UML class diagram with associations (1 Flight → many Seats, 1 Booking → 1 Passenger).

---

## 3. How do you prevent double-booking of seats?

* **Locking strategy**:

    * In-memory → `ReentrantLock` per flight.
    * Database → row-level locking or optimistic locking with version column.
* Atomic seat reservation → payment → confirmation/rollback.

👉 **Answer Tip**: State that consistency > availability for bookings.

---

## 4. How do you design the booking flow?

1. Passenger request → validate.
2. Lock flight seat map → reserve seat.
3. Create booking (PENDING).
4. Process payment via strategy.
5. On success → CONFIRMED, else rollback seat & mark FAILED.

👉 **Answer Tip**: Show sequence diagram with PaymentProcessor + NotificationService.

---

## 5. How do you handle payment integration?

* Use **Strategy Pattern** for multiple processors.
* Use **Adapter Pattern** for third-party SDKs (e.g., Stripe).
* Support retries & idempotency.
* PCI compliance: store tokens, not raw card data.

---

## 6. What design patterns are used and why?

* **Repository** → decouple storage.
* **Factory** → build aircraft seat templates.
* **Strategy** → payments, seat allocation.
* **Adapter** → external gateways.
* **Observer** → notifications, loyalty.
* **Builder** → booking creation.

👉 **Answer Tip**: Mention extensibility as the main benefit.

---

## 7. How would you model seat availability?

* Option 1: One row per seat (strong control, fine-grained locking).
* Option 2: Seat-class availability count (simpler, less detailed).
* Trade-off: detailed seat maps give flexibility, but cost more in storage & concurrency handling.

---

## 8. How do you ensure idempotency in bookings?

* Generate **idempotency key** per booking request.
* Store in DB → if same request arrives, return existing booking.
* Prevents double payment when user retries.

---

## 9. How would you extend to loyalty programs?

* Add `LoyaltyService` as **Observer** → listens to `BookingConfirmed` event.
* Award points to passenger’s account.
* Extend booking model with `loyaltyPointsUsed`.

---

## 10. How to support multi-leg journeys?

* Introduce **Itinerary** entity → list of `FlightLeg`s.
* Booking references Itinerary instead of a single Flight.
* Ensure atomic seat reservations across all legs (all-or-nothing).

---

## 11. What are key APIs for this system?

* `POST /flights` → create flight.
* `GET /flights?src=DEL&dst=BOM` → search flights.
* `POST /bookings` → book seat (with passenger & payment info).
* `GET /bookings/{id}` → booking status.
* `DELETE /bookings/{id}` → cancel booking.

---

## 12. What database design would you use?

* Relational DB (PostgreSQL, MySQL):

    * `Flights(flight_no, src, dst, departure, arrival, aircraft_id)`
    * `Aircraft(aircraft_id, model)`
    * `Seats(seat_id, flight_no, seat_no, class, available)`
    * `Passengers(passenger_id, name, email)`
    * `Bookings(booking_id, flight_no, passenger_id, seat_id, status, tx_id)`

👉 **Answer Tip**: Stress relational DB for strong consistency.

---

## 13. How do you handle cancellations and refunds?

* Change booking status → CANCELLED.
* Release seat back to pool.
* Call PaymentProcessor → refund transaction.
* Notify passenger via NotificationService.

---

## 14. How do you make the system scalable?

* Read-heavy → cache flight schedules & seat maps.
* Write-heavy (booking) → use DB transactions or distributed locks.
* Horizontally scale BookingService with idempotency keys.
* Event-driven architecture for async tasks (loyalty, notifications).

---

## 15. What failure scenarios must you handle?

* Payment succeeded but booking failed (compensation).
* Seat reserved but payment declined (rollback).
* Network failure during booking → retry with idempotency key.
* Flight canceled → rebook passengers or refund.

👉 **Answer Tip**: Stress **atomicity** & **compensating transactions**.

---

# 📑 Slide-friendly format

Use this outline for presentation (each `##` becomes a slide):

```markdown
# Airline Management System – Key Points

## Core Use Cases
- Manage flights, aircraft, passengers
- Book seats, assign, pay
- Cancel & refund
- Extensibility: loyalty, meals, multi-leg

## Entities & Relationships
- Flight ↔ Aircraft ↔ Seat
- Booking ↔ Passenger
- Payment linked to Booking

## Prevent Double Booking
- Lock per flight / DB row
- Atomic seat reserve + payment
- Rollback on failure

## Booking Flow
1. Validate passenger & flight
2. Lock + reserve seat
3. Create pending booking
4. Process payment
5. Confirm or rollback

## Payment Integration
- Strategy + Adapter
- Retry & idempotency
- PCI compliance

## Design Patterns
- Repository, Factory, Strategy
- Adapter, Observer, Builder

## Seat Availability Modeling
- Per-seat vs seat-class counts
- Trade-offs: detail vs simplicity

## Idempotency
- Unique request key
- Return existing booking if retried

## Loyalty & Extensibility
- Observer → award points
- Extend Booking with extras

## Multi-leg Journeys
- Itinerary entity
- Atomic seat reservations

## Key APIs
- POST /bookings
- GET /flights
- GET /bookings/{id}
- DELETE /bookings/{id}

## Database Design
- Flights, Aircraft, Seats
- Passengers, Bookings
- Enforce constraints with FK

## Cancellations & Refunds
- Update status → CANCELLED
- Release seat
- Refund via PaymentProcessor

## Scalability
- Cache for reads
- DB transactions for writes
- Event-driven async flows

## Failure Scenarios
- Payment mismatch
- Seat rollback
- Network retry with idempotency
- Flight cancel → refund/rebook
```

---
