package com.upisimulator.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String requesterUpi;
    private final String requestedFromUpi;
    private final BigDecimal amount;
    private final LocalDateTime createdAt;
    private boolean paid;

    public PaymentRequest(String requesterUpi, String requestedFromUpi, BigDecimal amount) {
        this.requesterUpi = requesterUpi;
        this.requestedFromUpi = requestedFromUpi;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
        this.paid = false;
    }

    public String getRequesterUpi() { return requesterUpi; }
    public String getRequestedFromUpi() { return requestedFromUpi; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isPaid() { return paid; }
    public void markPaid() { paid = true; }
}
