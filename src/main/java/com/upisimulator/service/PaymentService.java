package com.upisimulator.service;

import com.upisimulator.exception.*;
import com.upisimulator.model.*;
import com.upisimulator.util.DataStore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PaymentService {
    private static final BigDecimal DAILY_LIMIT = new BigDecimal("10000.00");
    private static final BigDecimal MAX_OPENING_BALANCE = new BigDecimal("1000000.00");
    private final DataStore store;

    public PaymentService(DataStore store) {
        this.store = store;
    }

    public synchronized UpiUser register(String name, String upiId, String pin,
                                         BigDecimal openingBalance) throws Exception {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Enter your full name.");
        String normalized = normalizeUpi(upiId);
        if (!normalized.matches("[a-z0-9._-]{3,}@[a-z0-9.-]{2,}")) {
            throw new IllegalArgumentException("Use a UPI ID like name@upi.");
        }
        if (store.getUsers().containsKey(normalized)) {
            throw new IllegalArgumentException("This UPI ID is already registered.");
        }
        if (pin == null || !pin.matches("\\d{4,6}")) {
            throw new IllegalArgumentException("PIN must contain 4 to 6 digits.");
        }
        validateAmount(openingBalance);
        if (openingBalance.compareTo(MAX_OPENING_BALANCE) > 0) {
            throw new IllegalArgumentException("Opening balance cannot exceed Rs. 10,00,000.");
        }

        UpiUser user = new UpiUser(name.trim(), normalized, pin,
                new BankAccount(normalized, openingBalance));
        store.getUsers().put(normalized, user);
        store.save();
        return user;
    }

    public synchronized UpiUser login(String upiId, String pin) throws Exception {
        String normalized = normalizeUpi(upiId);
        UpiUser user = store.getUsers().get(normalized);
        if (user == null) throw new IllegalArgumentException("No account found for this UPI ID.");
        if (user.isLocked()) throw new AccountLockedException("Account locked after 3 incorrect PIN attempts.");
        if (!user.getPin().equals(pin)) {
            user.recordFailedPinAttempt();
            store.save();
            if (user.isLocked()) throw new AccountLockedException("Account locked after 3 incorrect PIN attempts.");
            throw new InvalidUpiPinException("Incorrect PIN. Attempts left: " + (3 - user.getFailedPinAttempts()));
        }
        user.resetFailedPinAttempts();
        store.save();
        return user;
    }

    public synchronized UpiUser getUser(String upiId) {
        return store.getUsers().get(normalizeUpi(upiId));
    }

    public synchronized List<UpiUser> getAllUsers() {
        return new ArrayList<>(store.getUsers().values());
    }

    public synchronized BigDecimal getBalance(String upiId) {
        UpiUser user = requireUser(upiId);
        return user.getAccount().getBalance();
    }

    // Synchronization makes each transfer atomic inside this simulator.
    public synchronized Transaction sendMoney(String senderUpi, String receiverUpi,
                                              String pin, BigDecimal amount) throws Exception {
        String senderId = normalizeUpi(senderUpi);
        String receiverId = normalizeUpi(receiverUpi);
        UpiUser sender = requireUser(senderId);
        UpiUser receiver = requireUser(receiverId);

        if (senderId.equals(receiverId)) throw new IllegalArgumentException("You cannot send money to yourself.");
        validateAmount(amount);
        if (sender.isLocked()) throw new AccountLockedException("Your account is locked.");
        if (!sender.getPin().equals(pin)) {
            sender.recordFailedPinAttempt();
            store.save();
            if (sender.isLocked()) throw new AccountLockedException("Account locked after 3 incorrect PIN attempts.");
            throw new InvalidUpiPinException("Incorrect PIN. Attempts left: " + (3 - sender.getFailedPinAttempts()));
        }

        BigDecimal alreadySent = getTodayOutgoingTotal(senderId);
        if (alreadySent.add(amount).compareTo(DAILY_LIMIT) > 0) {
            Transaction failed = new Transaction(senderId, receiverId, amount,
                    TransactionType.TRANSFER, TransactionStatus.FAILED, "Daily limit exceeded");
            store.getTransactions().add(failed);
            store.save();
            throw new DailyLimitExceededException("Daily outgoing limit is Rs. 10,000.");
        }
        if (sender.getAccount().getBalance().compareTo(amount) < 0) {
            Transaction failed = new Transaction(senderId, receiverId, amount,
                    TransactionType.TRANSFER, TransactionStatus.FAILED, "Insufficient balance");
            store.getTransactions().add(failed);
            store.save();
            throw new InsufficientBalanceException("Insufficient balance. No money was deducted.");
        }

        // No balance is changed until all checks pass.
        sender.getAccount().debit(amount);
        receiver.getAccount().credit(amount);
        Transaction success = new Transaction(senderId, receiverId, amount,
                TransactionType.TRANSFER, TransactionStatus.SUCCESS, "Payment completed");
        store.getTransactions().add(success);
        sender.resetFailedPinAttempts();
        store.save();
        return success;
    }

    public synchronized PaymentRequest requestMoney(String requesterUpi, String fromUpi,
                                                    BigDecimal amount) throws Exception {
        String requester = normalizeUpi(requesterUpi);
        String from = normalizeUpi(fromUpi);
        requireUser(requester);
        requireUser(from);
        if (requester.equals(from)) throw new IllegalArgumentException("You cannot request money from yourself.");
        validateAmount(amount);
        PaymentRequest request = new PaymentRequest(requester, from, amount);
        store.getRequests().add(request);
        store.getTransactions().add(new Transaction(requester, from, amount,
                TransactionType.REQUEST, TransactionStatus.PENDING, "Money request created"));
        store.save();
        return request;
    }

    public synchronized List<Transaction> getTransactionsFor(String upiId) {
        String id = normalizeUpi(upiId);
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : store.getTransactions()) {
            if (t.getFromUpi().equals(id) || t.getToUpi().equals(id)) result.add(t);
        }
        result.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return result;
    }

    public synchronized List<Transaction> getMiniStatement(String upiId, int count) {
        List<Transaction> all = getTransactionsFor(upiId);
        return all.subList(0, Math.min(Math.max(count, 0), all.size()));
    }

    private BigDecimal getTodayOutgoingTotal(String upiId) {
        BigDecimal total = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();
        for (Transaction t : store.getTransactions()) {
            if (t.getType() == TransactionType.TRANSFER
                    && t.getStatus() == TransactionStatus.SUCCESS
                    && t.getFromUpi().equals(upiId)
                    && t.getTimestamp().toLocalDate().equals(today)) {
                total = total.add(t.getAmount());
            }
        }
        return total;
    }

    private UpiUser requireUser(String upiId) {
        UpiUser user = store.getUsers().get(normalizeUpi(upiId));
        if (user == null) throw new IllegalArgumentException("UPI ID not found: " + upiId);
        return user;
    }

    private void validateAmount(BigDecimal amount) throws InvalidAmountException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
        if (amount.scale() > 2) {
            throw new InvalidAmountException("Amount can have at most 2 decimal places.");
        }
    }

    private String normalizeUpi(String upiId) {
        return upiId == null ? "" : upiId.trim().toLowerCase(Locale.ROOT);
    }
}
