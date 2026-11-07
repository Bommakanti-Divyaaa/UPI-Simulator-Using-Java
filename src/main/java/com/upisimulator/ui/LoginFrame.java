package com.upisimulator.ui;

import com.upisimulator.model.UpiUser;
import com.upisimulator.service.PaymentService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class LoginFrame extends JFrame {
    private final PaymentService service;
    private final JTextField upiField = new JTextField(18);
    private final JPasswordField pinField = new JPasswordField(18);

    public LoginFrame(PaymentService service) {
        this.service = service;
        setTitle("UPI Payment Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 330);
        setLocationRelativeTo(null);

        JLabel title = new JLabel("UPI Payment Simulator", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        JLabel subtitle = new JLabel("Virtual payments • Java Swing", SwingConstants.CENTER);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("UPI ID:"));
        form.add(upiField);
        form.add(new JLabel("UPI PIN:"));
        form.add(pinField);

        JButton login = new JButton("Login");
        JButton register = new JButton("Create Account");
        login.addActionListener(e -> doLogin());
        register.addActionListener(e -> doRegister());

        JPanel buttons = new JPanel();
        buttons.add(login);
        buttons.add(register);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        center.add(title, BorderLayout.NORTH);
        center.add(form, BorderLayout.CENTER);
        center.add(buttons, BorderLayout.SOUTH);

        add(subtitle, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(new JLabel("Educational simulator only — no real money is used.", SwingConstants.CENTER), BorderLayout.SOUTH);
    }

    private void doLogin() {
        try {
            UpiUser user = service.login(upiField.getText(), new String(pinField.getPassword()));
            dispose();
            new DashboardFrame(service, user).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doRegister() {
        JTextField name = new JTextField();
        JTextField upi = new JTextField();
        JPasswordField pin = new JPasswordField();
        JTextField balance = new JTextField("1000.00");
        Object[] fields = {
                "Full name:", name,
                "New UPI ID (example: name@upi):", upi,
                "Create 4–6 digit PIN:", pin,
                "Opening virtual balance (Rs.):", balance
        };
        int result = JOptionPane.showConfirmDialog(this, fields, "Create UPI Account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        try {
            service.register(name.getText(), upi.getText(), new String(pin.getPassword()),
                    new BigDecimal(balance.getText().trim()));
            JOptionPane.showMessageDialog(this, "Account created successfully. You can now log in.");
            upiField.setText(upi.getText());
            pinField.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
