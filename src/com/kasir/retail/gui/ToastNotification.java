package com.kasir.retail.gui;

import javax.swing.*;
import java.awt.*;

public class ToastNotification {

    public static void show(JComponent parent, String message, Color bgColor, int durationMs) {
        JDialog toast = new JDialog();
        toast.setUndecorated(true);
        toast.setAlwaysOnTop(true);
        toast.setFocusableWindowState(false);

        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.brighter(), 1, true),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));

        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        msgLabel.setForeground(Color.WHITE);
        panel.add(msgLabel, BorderLayout.CENTER);

        toast.getContentPane().add(panel);
        toast.pack();

        Point p = parent.getLocationOnScreen();
        int x = p.x + (parent.getWidth() - toast.getWidth()) / 2;
        int y = p.y + 60;
        toast.setLocation(x, y);
        toast.setOpacity(0.0f);

        new Thread(() -> {
            try {
                for (float i = 0; i <= 1.0; i += 0.1f) {
                    final float opacity = i;
                    SwingUtilities.invokeLater(() -> toast.setOpacity(opacity));
                    Thread.sleep(30);
                }
                Thread.sleep(durationMs);
                for (float i = 1.0f; i >= 0; i -= 0.1f) {
                    final float opacity = i;
                    SwingUtilities.invokeLater(() -> toast.setOpacity(opacity));
                    Thread.sleep(30);
                }
                SwingUtilities.invokeLater(toast::dispose);
            } catch (InterruptedException ignored) { }
        }).start();

        toast.setVisible(true);
    }

    public static void showSuccess(JComponent parent, String message) {
        show(parent, message, new Color(0, 201, 167), 2000);
    }

    public static void showError(JComponent parent, String message) {
        show(parent, message, new Color(255, 82, 82), 2500);
    }

    public static void showInfo(JComponent parent, String message) {
        show(parent, message, new Color(52, 152, 255), 2000);
    }
}
