package com.upisimulator.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String transactionId;
    private final String fromUpi;
    private final String toUpi;
    private final BigDecimal amount;
    private final TransactionType type;
    private final TransactionStatus status;
    private final String note;
    private final LocalDateTime timestamp;

    public Transaction(String fromUpi, String toUpi, BigDecimal amount,
                       TransactionType type, TransactionStatus status, String note) {
        this.transactionId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.fromUpi = fromUpi;
        this.toUpi = toUpi;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.note = note == null ? "" : note;
        this.timestamp = LocalDateTime.now();
    }

    public String getTransactionId() { return transactionId; }
    public String getFromUpi() { return fromUpi; }
    public String getToUpi() { return toUpi; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public String getNote() { return note; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return timestamp.format(FORMAT) + " | " + type + " | " + fromUpi + " -> "
                + toUpi + " | Rs. " + amount.toPlainString() + " | " + status
                + (note.isBlank() ? "" : " | " + note);
    }
}
