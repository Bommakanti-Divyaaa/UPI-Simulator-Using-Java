package com.upisimulator.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class BankAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String upiId;
    private BigDecimal balance;

    public BankAccount(String upiId, BigDecimal openingBalance) {
        this.upiId = upiId;
        this.balance = openingBalance;
    }

    public String getUpiId() { return upiId; }
    public BigDecimal getBalance() { return balance; }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        balance = balance.subtract(amount);
    }
}
