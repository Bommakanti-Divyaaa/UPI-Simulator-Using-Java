package com.upisimulator.ui;

import com.upisimulator.model.Transaction;
import com.upisimulator.model.UpiUser;
import com.upisimulator.service.PaymentService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DashboardFrame extends JFrame {
    private final PaymentService service;
    private final UpiUser user;
    private final JLabel balanceLabel = new JLabel();
    private final JTextArea historyArea = new JTextArea();

    public DashboardFrame(PaymentService service, UpiUser user) {
        this.service = service;
        this.user = user;
        setTitle("UPI Simulator - " + user.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 560);
        setLocationRelativeTo(null);

        JLabel heading = new JLabel("Welcome, " + user.getFullName() + "  (" + user.getUpiId() + ")");
        heading.setFont(new Font("SansSerif", Font.BOLD, 18));
        JPanel top = new JPanel(new BorderLayout(10, 8));
        top.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        top.add(heading, BorderLayout.NORTH);
        balanceLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        top.add(balanceLabel, BorderLayout.SOUTH);

        JButton send = new JButton("Send Money");
        JButton request = new JButton("Request Money");
        JButton refresh = new JButton("Refresh Balance");
        JButton mini = new JButton("Mini Statement");
        JButton logout = new JButton("Logout");

        send.addActionListener(e -> sendMoney());
        request.addActionListener(e -> requestMoney());
        refresh.addActionListener(e -> refreshBalance());
        mini.addActionListener(e -> showMiniStatement());
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame(service).setVisible(true);
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actions.add(send);
        actions.add(request);
        actions.add(refresh);
        actions.add(mini);
        actions.add(logout);

        historyArea.setEditable(false);
        historyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(historyArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Transaction History"));

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        refreshBalance();
        showHistory();
    }

    private void sendMoney() {
        JTextField receiver = new JTextField();
        JTextField amount = new JTextField();
        JPasswordField pin = new JPasswordField();
        Object[] fields = {"Receiver UPI ID:", receiver, "Amount (Rs.):", amount, "Your UPI PIN:", pin};
        if (JOptionPane.showConfirmDialog(this, fields, "Send Money",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        // SwingWorker keeps the interface responsive and exercises Java concurrency.
        new SwingWorker<Transaction, Void>() {
            @Override protected Transaction doInBackground() throws Exception {
                return service.sendMoney(user.getUpiId(), receiver.getText(),
                        new String(pin.getPassword()), new BigDecimal(amount.getText().trim()));
            }
            @Override protected void done() {
                try {
                    Transaction t = get();
                    JOptionPane.showMessageDialog(DashboardFrame.this,
                            "Payment successful!\nTransaction ID: " + t.getTransactionId()
                                    + "\nAmount: " + money(t.getAmount()));
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    JOptionPane.showMessageDialog(DashboardFrame.this,
                            cause.getMessage(), "Payment failed", JOptionPane.ERROR_MESSAGE);
                }
                refreshBalance();
                showHistory();
            }
        }.execute();
    }

    private void requestMoney() {
        JTextField payer = new JTextField();
        JTextField amount = new JTextField();
        Object[] fields = {"Request money from UPI ID:", payer, "Amount (Rs.):", amount};
        if (JOptionPane.showConfirmDialog(this, fields, "Request Money",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            service.requestMoney(user.getUpiId(), payer.getText(), new BigDecimal(amount.getText().trim()));
            JOptionPane.showMessageDialog(this, "Money request recorded. No money has been transferred.");
            showHistory();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Request failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshBalance() {
        balanceLabel.setText("Available balance: " + money(service.getBalance(user.getUpiId())));
    }

    private void showMiniStatement() {
        List<Transaction> list = service.getMiniStatement(user.getUpiId(), 5);
        StringBuilder text = new StringBuilder("Last " + list.size() + " transactions:\n\n");
        for (Transaction t : list) text.append(t).append('\n');
        JOptionPane.showMessageDialog(this, text.toString(), "Mini Statement", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHistory() {
        List<Transaction> list = service.getTransactionsFor(user.getUpiId());
        StringBuilder text = new StringBuilder();
        if (list.isEmpty()) text.append("No transactions yet.\n");
        for (Transaction t : list) text.append(t).append('\n');
        historyArea.setText(text.toString());
    }

    private String money(BigDecimal amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return "Rs. " + nf.format(amount);
    }
}
