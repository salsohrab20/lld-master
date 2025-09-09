1. **Class diagram** (shows classes, interfaces, attributes & major methods, and relationships)
2. **Sequence diagram** for the main booking flow (reserve seat → create booking → process payment → confirm/rollback)

I’ll also add a short mapping from the UML elements to the Java classes I provided earlier and a few extension notes.

---

# Class diagram (PlantUML)
![img_1.png](img_1.png)

```plantuml
@startuml
' --- Legend / skin
skinparam classAttributeIconSize 0
skinparam shadowing false
' --- Interfaces
interface PaymentProcessor {
  +process(request: PaymentRequest) : PaymentResult
}
interface Repository<T,ID> {
  +save(t:T) : T
  +findById(id:ID) : Optional<T>
  +findAll() : List<T>
  +deleteById(id:ID) : void
}
interface NotificationService {
  +notifyBooking(b: Booking, message: String) : void
}

' --- Enums
enum SeatClass
enum BookingStatus

' --- Domain classes
class Aircraft {
  - model: String
  - seatTemplate: Map<String,SeatClass>
  + addSeatTemplate(seatNo:String, sc:SeatClass): void
  + getSeatTemplate(): Map<String,SeatClass>
}

class Seat {
  - seatNumber: String
  - seatClass: SeatClass
  - available: boolean
  + isAvailable(): boolean
  + reserve(): boolean
  + release(): void
}

class Flight {
  - flightNumber: String
  - source: String
  - destination: String
  - departure: LocalDateTime
  - arrival: LocalDateTime
  - seats: Map<String,Seat>
  - lock: ReentrantLock
  + initializeSeatsFromAircraft(a:Aircraft): void
  + findAvailableSeat(sc:SeatClass): Optional<Seat>
  + findSeat(seatNo:String): Optional<Seat>
}

class Passenger {
  - id: String
  - name: String
  - email: String
  + getters / setters
}

class Booking {
  - id: String
  - flightNumber: String
  - passengerId: String
  - seatNumber: String
  - status: BookingStatus
  - amount: BigDecimal
  - paymentTransactionId: String
  - createdAt: Instant
  + (Builder pattern) ...
  + getters / setters
}

class PaymentRequest {
  - amount: BigDecimal
  - currency: String
  - metadata: Map<String,String>
}

class PaymentResult {
  - success: boolean
  - transactionId: String
  - message: String
}

' --- Services / higher level
class AircraftFactory {
  + createA320() : Aircraft
}

class BookingService {
  - flightRepo: Repository<Flight,String>
  - passengerRepo: Repository<Passenger,String>
  - bookingRepo: Repository<Booking,String>
  - processors: Map<String,PaymentProcessor>
  - notifier: NotificationService
  + registerProcessor(key:String, p:PaymentProcessor): void
  + book(passengerId:String, flightNumber:String, sc:SeatClass, amount:BigDecimal, paymentMethod:String, paymentMeta:Map): Booking
  + getBooking(id:String): Optional<Booking>
}

class ConsoleNotificationService {
  + notifyBooking(b:Booking, message:String): void
}

' --- Payment implementations
class CreditCardProcessor
class PayPalProcessor
class StripeAdapter {
  - gateway: StripeGateway
  + process(req: PaymentRequest) : PaymentResult
}
class StripeGateway {
  + charge(cardToken:String, amount:String) : String
}

' --- Repositories concrete
class InMemoryRepository<T,ID> {
  - store: Map<ID,T>
  - idExtractor: Function<T,ID>
  + save(t:T): T
  + findById(id:ID): Optional<T>
  + findAll(): List<T>
  + deleteById(id:ID): void
}

' --- Relationships
PaymentProcessor <|.. CreditCardProcessor
PaymentProcessor <|.. PayPalProcessor
PaymentProcessor <|.. StripeAdapter
StripeAdapter ..> StripeGateway : uses

Repository <|.. InMemoryRepository
Repository ..> Flight
Repository ..> Passenger
Repository ..> Booking

BookingService ..> Repository : uses
BookingService ..> PaymentProcessor : uses
BookingService ..> NotificationService : notifies
BookingService ..> Flight : reads/updates
BookingService ..> Seat : reserves/releases
BookingService ..> Booking : creates/updates
BookingService ..> Passenger : reads

Flight "1" o-- "many" Seat : contains
Aircraft "1" --> "many" Seat : template (copied to Flight)
Booking "1" --> "1" Passenger
Booking "1" --> "1" Flight

@enduml
```

---

# Sequence diagram (PlantUML)

![img.png](img.png)

```plantuml
@startuml
actor User
participant "BookingService" as BS
participant "PassengerRepo" as PR
participant "FlightRepo" as FR
participant "Flight" as F
participant "Seat" as S
participant "BookingRepo" as BR
participant "PaymentProcessor" as PP
participant "NotificationService" as NS

User -> BS : book(passengerId, flightNo, seatClass, amount, paymentMethod, meta)
BS -> PR : findById(passengerId)
PR --> BS : Passenger
BS -> FR : findById(flightNo)
FR --> BS : Flight
BS -> F : getLock() / lock()
BS -> F : findAvailableSeat(seatClass)
F --> BS : Seat (available)
BS -> S : reserve()
S --> BS : success
BS -> BR : save(Booking status=PENDING)
BR --> BS : saved Booking
BS -> PP : process(PaymentRequest)
PP --> BS : PaymentResult(success/failed, txId)
alt payment success
    BS -> Booking : setStatus(CONFIRMED), setTxId(...)
    BS -> BR : save(Booking)
    BS -> NS : notifyBooking(CONFIRMED)
    NS --> BS : ack
    BS --> User : Booking (CONFIRMED)
else payment failure
    BS -> F : findSeat(seatNo) -> Seat.release()
    BS -> Booking : setStatus(FAILED)
    BS -> BR : save(Booking)
    BS -> NS : notifyBooking(FAILED)
    NS --> BS : ack
    BS --> User : Booking (FAILED)
end
@enduml
```

---

# Mapping of UML elements → Java files (quick)

* `BookingService` → `com.example.airline.service.BookingService`
* `Flight`, `Seat`, `Aircraft` → `com.example.airline.model.*`
* `Booking`, `Passenger` → `com.example.airline.model.*`
* `PaymentProcessor`, implementations → `com.example.airline.payment.*`
* `Repository` / `InMemoryRepository` → `com.example.airline.repo.*`
* `NotificationService`, `ConsoleNotificationService` → `com.example.airline.service.*`
* `AircraftFactory` → `com.example.airline.service.AircraftFactory`

---

# Notes & extension suggestions (for the diagram)

* If you add a **LoyaltyService** later, model it as an Observer: `BookingService` will emit an event or call `LoyaltyService.awardPoints(Booking)` after confirmation. In UML add `<<observer>> LoyaltyService` and a dependency arrow from `BookingService`.
* For **multi-leg itineraries**, introduce `Itinerary` composed of `FlightLeg` (each leg points to `Flight`) and show `Booking` referencing `Itinerary` (composition).
* For DB-backed implementation, replace `InMemoryRepository` with `JpaRepository` (stereotype `<<repository>>`) and show `BookingService` calls wrapped in a transaction (annotate method with `<<transaction>>`).

---
