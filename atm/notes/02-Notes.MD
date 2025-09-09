    
```plantuml
┌──────────────────┐
                   │       ATM        │  (Singleton + Facade)
                   └──────────────────┘
                        | uses
                        v
┌─────────────────┐   ┌──────────────────┐   ┌──────────────────┐
│    Card         │   │   BankService    │<--│ MockBankService   │
│ cardNumber      │   │ + authenticate() │   └──────────────────┘
│ pin             │   │ + getAccount()   │
│ accountNumber   │   └──────────────────┘
└─────────────────┘
|
v
┌─────────────────┐
│    Account      │
│ + getBalance()  │
│ + deposit()     │
│ + withdraw()    │
└─────────────────┘
|
v
┌─────────────────┐
│ CashDispenser   │
│ + dispenseCash()│
│ + addCash()     │
└─────────────────┘

       ┌─────────────────────────────────────────┐
       │              Transaction                │ (Strategy Pattern)
       │ + execute()                             │
       └─────────────────────────────────────────┘
                 ▲               ▲             ▲
                 │               │             │
                 │               │             │
┌──────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ BalanceInquiry    │ │    Deposit      │ │   Withdrawal     │
└──────────────────┘ └─────────────────┘ └─────────────────┘
```

```text

User          ATM           BankService         Account          CashDispenser
|             |                |                  |                   |
| Insert Card |                |                  |                   |
|------------>|                |                  |                   |
| Enter PIN   |                |                  |                   |
|------------>| authenticate() |                  |                   |
|             |--------------->|                  |                   |
|             |<---------------|                  |                   |
|             |                |                  |                   |
|  Select Withdrawal           |                  |                   |
|------------>| createTxn()    |                  |                   |
|             |---------------------------------->Account retrieved   |
|             |                |                  |                   |
|             | execute()      |                  |                   |
|             |--------------->| getAccount()     |                   |
|             |                |----------------->| check balance     |
|             |                |<-----------------| balance ok        |
|             |                |                  |                   |
|             | withdraw()     |                  |                   |
|             |---------------------------------->| deduct balance    |
|             |                |                  | return success    |
|             |                |                  |------------------>|
|             |                |                  | dispense cash     |
|             |                |                  |<------------------|
|             |<---------------------------------| confirm withdrawal|
|<------------| print receipt  |                  |                   |

```
---

# 🔟 Follow-Up Interview Questions & Model Answers

---

### **1. Why did you make ATM a Singleton?**

👉 **Answer:**
An ATM machine represents a single physical unit. Having multiple instances in memory for the same machine doesn’t make sense. The **Singleton pattern** ensures only one instance exists per ATM machine and provides a global access point.

---

### **2. Why use the Strategy pattern for transactions?**

👉 **Answer:**
Transactions like `BalanceInquiry`, `Deposit`, and `Withdrawal` share a common structure but have different behaviors.
Using the **Strategy pattern** (via `Transaction` abstract class) allows us to add new features (like Fund Transfer or Mini-Statement) without modifying existing code → follows **Open/Closed Principle**.

---

### **3. How do you ensure thread safety in your design?**

👉 **Answer:**

* `Account`’s `deposit()` and `withdraw()` methods are `synchronized`.
* `CashDispenser`’s methods are also synchronized.
* Withdrawals use **atomic operations** (balance check + deduction + cash dispense inside a synchronized block).
  This prevents race conditions when multiple users attempt concurrent transactions.

---

### **4. What if ATM runs out of cash but the account has enough balance?**

👉 **Answer:**
The `Withdrawal` transaction first checks both **Account balance** and **CashDispenser inventory**.
If the ATM doesn’t have enough physical cash, the withdrawal is denied with a proper message.
This ensures consistency between bank balance and ATM cash availability.

---

### **5. How would you extend this system for multi-currency support?**

👉 **Answer:**
Introduce a `Currency` entity and update `CashDispenser` to maintain multiple denominations per currency.
Transactions would then work with `Currency` objects instead of raw amounts.
This keeps the system **extensible** without breaking existing functionality.

---

### **6. How would you log transactions or provide mini-statements?**

👉 **Answer:**
We can maintain a `TransactionLog` class or service.
Every `Transaction.execute()` writes an entry into the log (date, type, amount, account).
For mini-statements, we just fetch the latest N entries.
This follows **Single Responsibility** and can use the **Observer pattern** to notify when a transaction occurs.

---

### **7. Why not let the ATM directly manage Account balances instead of BankService?**

👉 **Answer:**
The ATM is just an interface to the banking system.
Only the **BankService** should manage account validation and balances to ensure consistency across multiple ATMs and digital channels (like online banking).
This separation of concerns makes the design realistic and scalable.

---

### **8. How do you handle concurrent access when two ATMs withdraw from the same account?**

👉 **Answer:**
The critical section is inside the **BankService/Account layer**, not the ATM.
We synchronize `Account.withdraw()` so even if two ATMs request withdrawal at the same time, only one succeeds if the balance allows.
This ensures **strong consistency** of account data.

---

### **9. How would you add biometric authentication instead of PIN?**

👉 **Answer:**

* Modify `BankService.authenticate()` to accept different authentication strategies (PIN, biometric, OTP).
* Use the **Strategy pattern** for authentication as well.
  This makes the system flexible to support multiple authentication methods.

---

### **10. What SOLID principles are applied in your design?**

👉 **Answer:**

* **S**ingle Responsibility → Each class (ATM, Account, CashDispenser, Transaction) has one responsibility.
* **O**pen/Closed → New transactions can be added without changing existing ones.
* **L**iskov Substitution → Any `Transaction` subclass can replace the abstract parent.
* **I**nterface Segregation → BankService is a clean interface, not overloaded.
* **D**ependency Inversion → ATM depends on `BankService` interface, not concrete implementation.

---


# 🎤 ATM System – 2-Minute Pitch (Interview Ready)

\*\*"I designed the ATM system by first identifying the core requirements: authentication, balance inquiry, deposit, withdrawal, cash inventory management, and transaction recording, with a focus on concurrency and extensibility.

I modeled the key entities as classes: `ATM`, `Card`, `Account`, `Transaction`, `CashDispenser`, and `BankService`.
The ATM acts as a **facade** that orchestrates user interactions, while the `BankService` validates authentication and retrieves accounts. Each account supports **thread-safe** deposit and withdrawal to ensure consistency under concurrency.

For operations, I applied the **Strategy Pattern** via a `Transaction` hierarchy, where `BalanceInquiry`, `Deposit`, and `Withdrawal` are concrete strategies. This makes the system easily extensible – new features like fund transfer or mini-statements can be added without modifying existing logic.

The ATM itself is implemented as a **Singleton**, since only one instance per machine is needed. The **CashDispenser** manages ATM’s physical cash inventory, and withdrawals are handled atomically by synchronizing account updates with cash dispensing.

I also modeled the flow using a **sequence diagram** for withdrawals, showing how the ATM coordinates between the user, bank service, account, and cash dispenser.

This design ensures separation of concerns, thread safety, and extensibility, while following SOLID principles and common design patterns."\*\*

---