# Project Guide / Viva Notes

## Problem statement
Users need to simulate safe digital money transfers while avoiding insufficient-balance transfers, duplicate concurrent transfers, and daily-limit violations. This application models these rules with Java classes, exceptions, synchronization, and persistent transaction history.

## Objectives
- Build a GUI-based UPI-style simulator in Java.
- Register users with unique UPI IDs and PINs.
- Send and request virtual money.
- Validate balance, PIN, and daily transfer limits.
- Record successful, failed, and pending transactions.
- Demonstrate synchronized methods and background work using SwingWorker.
- Store data between application runs.

## Package structure
- `com.upisimulator.model`: UpiUser, BankAccount, Transaction, PaymentRequest, enums.
- `com.upisimulator.service`: PaymentService (business rules and synchronized operations).
- `com.upisimulator.exception`: custom exceptions.
- `com.upisimulator.ui`: login and dashboard windows.
- `com.upisimulator.util`: local file persistence.

## Main class responsibilities
- `UpiUser`: user profile and PIN attempt lockout.
- `BankAccount`: stores balance and credits/debits it.
- `Transaction`: transaction ID, sender, receiver, amount, status and timestamp.
- `PaymentService`: registration, login, send/request money, limits and transaction history.
- `DataStore`: loads and saves local objects.
- `LoginFrame`: login and registration GUI.
- `DashboardFrame`: balance, send, request, history and mini statement GUI.

## Transfer algorithm
1. Receive sender UPI ID, receiver UPI ID, PIN and amount.
2. Find both users.
3. Reject unknown users or self-transfer.
4. Check amount is positive and PIN is correct.
5. Check daily limit and sender balance.
6. If any check fails, record a failed transaction where appropriate and do not change balances.
7. Debit sender and credit receiver within one synchronized method.
8. Record a SUCCESS transaction and save data.
9. Display the result to the user.

## Concurrency
`sendMoney` is `synchronized`, so two transfer operations using this service instance cannot interleave their balance checks and updates. The UI also uses `SwingWorker` so a transfer does not freeze the Swing event-dispatch thread.

## Limitations / future enhancements
- Replace serialized file storage with MySQL/JDBC.
- Use secure PIN hashing and never display/store plain-text PINs.
- Add transaction cancellation and request acceptance.
- Add a proper account lock reset/admin flow.
- Add automated unit tests and a more polished UI.
- A real UPI app requires authorized payment-network and bank integrations; this student app does not provide those.
