package com.upisimulator.util;

import com.upisimulator.model.PaymentRequest;
import com.upisimulator.model.Transaction;
import com.upisimulator.model.UpiUser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {
    private final Path dataPath = Path.of("data", "upi-data.ser");
    private Map<String, UpiUser> users = new HashMap<>();
    private List<Transaction> transactions = new ArrayList<>();
    private List<PaymentRequest> requests = new ArrayList<>();

    public DataStore() {
        load();
    }

    public Map<String, UpiUser> getUsers() { return users; }
    public List<Transaction> getTransactions() { return transactions; }
    public List<PaymentRequest> getRequests() { return requests; }

    public synchronized void save() {
        try {
            Files.createDirectories(dataPath.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(dataPath)))) {
                out.writeObject(users);
                out.writeObject(transactions);
                out.writeObject(requests);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save local data: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized void load() {
        if (!Files.exists(dataPath)) return;
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(dataPath)))) {
            users = (Map<String, UpiUser>) in.readObject();
            transactions = (List<Transaction>) in.readObject();
            requests = (List<PaymentRequest>) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.err.println("Could not load saved data. Starting with empty data: " + e.getMessage());
            users = new HashMap<>();
            transactions = new ArrayList<>();
            requests = new ArrayList<>();
        }
    }
}
