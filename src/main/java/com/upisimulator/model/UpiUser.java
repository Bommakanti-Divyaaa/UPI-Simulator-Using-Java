package com.upisimulator.model;

import java.io.Serializable;

public class UpiUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String fullName;
    private final String upiId;
    // Educational simulator only. Never store real payment PINs as plain text.
    private final String pin;
    private final BankAccount account;
    private int failedPinAttempts;
    private boolean locked;

    public UpiUser(String fullName, String upiId, String pin, BankAccount account) {
        this.fullName = fullName;
        this.upiId = upiId;
        this.pin = pin;
        this.account = account;
    }

    public String getFullName() { return fullName; }
    public String getUpiId() { return upiId; }
    public String getPin() { return pin; }
    public BankAccount getAccount() { return account; }
    public int getFailedPinAttempts() { return failedPinAttempts; }
    public boolean isLocked() { return locked; }

    public void recordFailedPinAttempt() {
        failedPinAttempts++;
        if (failedPinAttempts >= 3) locked = true;
    }

    public void resetFailedPinAttempts() {
        failedPinAttempts = 0;
    }
}
