package com.kasir.retail;

import com.kasir.retail.database.DatabaseManager;
import com.kasir.retail.gui.LoginDialog;
import com.kasir.retail.gui.MainFrame;
import com.kasir.retail.gui.ThemeManager;
import com.kasir.retail.model.User;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {}
        }

        ThemeManager.applyFlatLafTweaks();

        DatabaseManager.initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            JFrame dummy = new JFrame();
            dummy.setUndecorated(true);
            dummy.setVisible(true);
            dummy.setLocationRelativeTo(null);

            LoginDialog login = new LoginDialog(dummy);
            dummy.dispose();

            User user = login.getLoggedInUser();

            if (user != null) {
                new MainFrame(user);
            } else {
                System.exit(0);
            }
        });
    }
}
