# Problem Statement    
- Design and implement an ATM (Automated Teller Machine) system that allows users to perform basic banking operations such as balance inquiry, cash withdrawal, and cash deposit, with secure authentication and proper cash management.

## Requirements    
```text
User Authentication: Users must authenticate using a card and PIN.    
Balance Inquiry: Users can check their account balance.    
Cash Withdrawal: Users can withdraw cash if sufficient balance and cash are available.    
Cash Deposit: Users can deposit cash into their account.    
Transaction Management: The system records and processes transactions (withdrawal, deposit).    
Banking Service Integration: The ATM interacts with a backend banking service to validate accounts and perform transactions.    
Cash Dispenser: The ATM manages its own cash inventory and dispenses cash securely.    
Concurrency & Consistency: The system handles concurrent access and ensures data consistency.    
User Interface: The ATM provides a user-friendly interface for operations.    
Extensibility: Easy to add new features such as mini-statements, fund transfers, or multi-currency support.
```
---
# 🚀 Step-by-Step Interview Approach

### 1. **Understand Requirements**

* Authentication via **Card + PIN**
* Operations: Balance inquiry, Deposit, Withdraw
* ATM should manage **its own cash inventory**
* Backend integration with **Bank service** for account validation
* Handle **concurrency & data consistency**
* Extensible for new features

---

### 2. **Identify Key Entities**

* **ATM** – main controller
* **Card** – contains cardNumber, expiry, etc.
* **Account** – balance, accountNumber
* **Transaction** – abstract class (Deposit, Withdraw, Inquiry extend it)
* **CashDispenser** – manages physical cash
* **BankService** – validates accounts, processes transactions
* **User Interface (UI)** – console/menu simulation

---

### 3. **Define Responsibilities**

* **ATM**: orchestrates flow (authentication → select operation → delegate to services).
* **BankService**: ensures correctness of balance/transactions.
* **CashDispenser**: ensures ATM has enough cash.
* **Transaction**: represents operations and logs them.
* **Concurrency Handling**: synchronize critical sections like withdrawal/deposit.

---

### 4. **Apply Design Patterns**

* **Strategy Pattern** – different transaction types (Withdraw, Deposit, Inquiry).
* **Factory Method** – create Transaction objects.
* **Singleton** – ATM instance.
* **Facade** – ATM acts as a facade hiding bank + dispenser complexities.
* **Observer/Publisher-Subscriber** (optional for notifications).

---

### 5. **Concurrency & Data Consistency**

* Use **synchronized methods** or **locks** when updating account balance or ATM cash.
* Ensure **atomicity** of withdrawal (check + deduct).

---

### 6. **Extensibility**

* Add new operations by creating new Transaction strategies.

---

# ✅ Core Java Implementation (Simplified)

```java
// ====== Model Classes ======

class Card {
    private String cardNumber;
    private String pin;
    private String accountNumber;

    public Card(String cardNumber, String pin, String accountNumber) {
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.accountNumber = accountNumber;
    }

    public String getCardNumber() { return cardNumber; }
    public String getPin() { return pin; }
    public String getAccountNumber() { return accountNumber; }
}

class Account {
    private String accountNumber;
    private double balance;

    public Account(String accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public synchronized double getBalance() {
        return balance;
    }

    public synchronized void deposit(double amount) {
        balance += amount;
    }

    public synchronized boolean withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }
}

// ====== Bank Service ======
interface BankService {
    boolean authenticate(Card card, String pin);
    Account getAccount(String accountNumber);
}

class MockBankService implements BankService {
    private Map<String, Account> accounts = new HashMap<>();
    private Map<String, String> cardPinMap = new HashMap<>();

    public MockBankService() {
        Account acc1 = new Account("A1001", 1000);
        accounts.put("A1001", acc1);
        cardPinMap.put("123456", "1111"); // card -> pin
    }

    @Override
    public boolean authenticate(Card card, String pin) {
        return cardPinMap.containsKey(card.getCardNumber()) &&
               cardPinMap.get(card.getCardNumber()).equals(pin);
    }

    @Override
    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }
}

// ====== Cash Dispenser ======
class CashDispenser {
    private int cashAvailable;

    public CashDispenser(int initialCash) {
        this.cashAvailable = initialCash;
    }

    public synchronized boolean dispenseCash(int amount) {
        if (cashAvailable >= amount) {
            cashAvailable -= amount;
            return true;
        }
        return false;
    }

    public synchronized void addCash(int amount) {
        cashAvailable += amount;
    }

    public int getCashAvailable() {
        return cashAvailable;
    }
}

// ====== Transaction (Strategy Pattern) ======
abstract class Transaction {
    protected Account account;
    protected BankService bankService;
    protected CashDispenser dispenser;

    public Transaction(Account account, BankService bankService, CashDispenser dispenser) {
        this.account = account;
        this.bankService = bankService;
        this.dispenser = dispenser;
    }

    public abstract void execute();
}

class BalanceInquiry extends Transaction {
    public BalanceInquiry(Account account, BankService bankService, CashDispenser dispenser) {
        super(account, bankService, dispenser);
    }
    @Override
    public void execute() {
        System.out.println("Balance: " + account.getBalance());
    }
}

class Deposit extends Transaction {
    private double amount;
    public Deposit(Account account, BankService bankService, CashDispenser dispenser, double amount) {
        super(account, bankService, dispenser);
        this.amount = amount;
    }
    @Override
    public void execute() {
        account.deposit(amount);
        dispenser.addCash((int) amount);
        System.out.println("Deposited: " + amount + ". New Balance: " + account.getBalance());
    }
}

class Withdrawal extends Transaction {
    private double amount;
    public Withdrawal(Account account, BankService bankService, CashDispenser dispenser, double amount) {
        super(account, bankService, dispenser);
        this.amount = amount;
    }
    @Override
    public void execute() {
        synchronized (account) { // ensure atomicity
            if (account.withdraw(amount) && dispenser.dispenseCash((int) amount)) {
                System.out.println("Withdrawn: " + amount + ". New Balance: " + account.getBalance());
            } else {
                System.out.println("Withdrawal failed. Insufficient balance or ATM cash.");
            }
        }
    }
}

// ====== ATM (Facade + Singleton) ======
class ATM {
    private static ATM instance;
    private BankService bankService;
    private CashDispenser dispenser;

    private ATM(BankService bankService, CashDispenser dispenser) {
        this.bankService = bankService;
        this.dispenser = dispenser;
    }

    public static ATM getInstance(BankService bankService, CashDispenser dispenser) {
        if (instance == null) {
            instance = new ATM(bankService, dispenser);
        }
        return instance;
    }

    public void start(Card card, String pin) {
        if (!bankService.authenticate(card, pin)) {
            System.out.println("Authentication failed!");
            return;
        }

        Account account = bankService.getAccount(card.getAccountNumber());
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\nChoose: 1. Balance 2. Deposit 3. Withdraw 4. Exit");
            int choice = sc.nextInt();
            if (choice == 4) break;

            Transaction txn = null;
            switch (choice) {
                case 1:
                    txn = new BalanceInquiry(account, bankService, dispenser);
                    break;
                case 2:
                    System.out.print("Enter amount: ");
                    double dep = sc.nextDouble();
                    txn = new Deposit(account, bankService, dispenser, dep);
                    break;
                case 3:
                    System.out.print("Enter amount: ");
                    double wd = sc.nextDouble();
                    txn = new Withdrawal(account, bankService, dispenser, wd);
                    break;
            }
            if (txn != null) txn.execute();
        }
        sc.close();
    }
}

// ====== Demo ======
public class ATMSystemDemo {
    public static void main(String[] args) {
        BankService bankService = new MockBankService();
        CashDispenser dispenser = new CashDispenser(5000);
        ATM atm = ATM.getInstance(bankService, dispenser);

        Card card = new Card("123456", "1111", "A1001");
        atm.start(card, "1111");
    }
}
```

---

# 📝 Key Takeaways for Interview

1. Start by clarifying requirements.
2. Identify entities and responsibilities.
3. Apply **OOP + design patterns**.
4. Show **thread safety & consistency**.
5. Discuss **extensibility** (adding mini-statement, fund transfer).

---
