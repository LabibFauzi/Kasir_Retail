package com.kasir.retail;

import com.kasir.retail.database.DatabaseManager;
import com.kasir.retail.gui.MainFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }
        DatabaseManager.initializeDatabase();
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
