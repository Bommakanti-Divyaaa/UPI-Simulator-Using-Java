package com.upisimulator;

import com.upisimulator.service.PaymentService;
import com.upisimulator.ui.LoginFrame;
import com.upisimulator.util.DataStore;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Use the default Swing look and feel if the system one is unavailable.
        }

        DataStore store = new DataStore();
        PaymentService service = new PaymentService(store);
        SwingUtilities.invokeLater(() -> new LoginFrame(service).setVisible(true));
    }
}
