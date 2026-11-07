# UPI Payment Simulator (Java Swing)

A B.Tech educational project that simulates UPI-style payments using virtual balances only.
It does **not** connect to banks, UPI networks, or real money.

## Requirements
- JDK 17 or later (JDK 11 may work)
- IntelliJ IDEA, Eclipse, or VS Code with Java extensions

## Run
1. Extract the ZIP.
2. Open the `UPIPaymentSimulator` folder in your IDE.
3. Run `src/main/java/com/upisimulator/Main.java`.
   - Or from the project folder, compile/run using the commands below.

### Compile and run from terminal
Windows:
```bat
javac -d out src\main\java\com\upisimulator\Main.java src\main\java\com\upisimulator\model\*.java src\main\java\com\upisimulator\service\*.java src\main\java\com\upisimulator\exception\*.java src\main\java\com\upisimulator\ui\*.java src\main\java\com\upisimulator\util\*.java
java -cp out com.upisimulator.Main
```

Linux/macOS:
```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out com.upisimulator.Main
```

## How to test
1. Register user A with UPI ID `a@upi`, PIN `1234`, opening balance `5000`.
2. Register user B with UPI ID `b@upi`, PIN `4321`, opening balance `1000`.
3. Log in as `a@upi`.
4. Choose **Send Money**, enter `b@upi`, amount `250`, and PIN `1234`.
5. Log in as `b@upi` to confirm the balance changed.
6. Open **Transactions** to see transaction records.

## Included features
- Registration and login
- UPI ID and PIN validation
- Balance check
- Send money and request money (creates a request record; does not debit money)
- Daily outgoing limit
- PIN attempt lockout after 3 consecutive failures
- Transaction history and mini statement
- Custom exceptions
- Synchronized transfer method for safe concurrent access
- Failed transfer is recorded without debiting either account
- File persistence in `data/upi-data.ser`

## Important educational notes
- This is a simulator, not a real payment app.
- The PIN is stored as a simple value in this student project to keep the example understandable. A real payment system must never store PINs this way; it needs secure authentication, encryption, server-side controls, audit logging, and regulatory compliance.
- Data is saved locally in a Java-serialized file. Only open data files you trust.
