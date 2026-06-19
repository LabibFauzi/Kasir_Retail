package com.kasir.retail.gui;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class QRISDialog extends JDialog {
    private static final int QR_SIZE = 280;

    public QRISDialog(JFrame parent, double total, String invoice) {
        super(parent, "Pembayaran QRIS", true);
        setSize(420, 500);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        try {
            setIconImage(new ImageIcon(getClass().getResource("/com/kasir/retail/gui/icon.png")).getImage());
        } catch (Exception ignored) {}

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Scan untuk Membayar", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(44, 62, 80));

        String qrContent = String.format("QRIS:Kasir Retail:%s:Rp%,.0f", invoice, total);
        BufferedImage qrImage = generateQRCode(qrContent, QR_SIZE);

        JLabel qrLabel = new JLabel(new ImageIcon(qrImage));
        qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
        qrLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel amountLabel = new JLabel(String.format("Total: Rp%,.0f", total), SwingConstants.CENTER);
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        amountLabel.setForeground(new Color(231, 76, 60));

        JLabel infoLabel = new JLabel(
            "<html><center>Scan QR code di atas menggunakan<br>aplikasi mobile banking atau e-wallet Anda</center></html>",
            SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(130, 130, 130));

        JButton closeBtn = ThemeManager.createButton("Tutup", ThemeManager.INFO);
        closeBtn.setPreferredSize(new Dimension(150, 38));
        closeBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(closeBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(qrLabel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 10));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(amountLabel, BorderLayout.NORTH);
        bottomPanel.add(infoLabel, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    private BufferedImage generateQRCode(String text, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);

            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, size, size);

            g.setColor(Color.BLACK);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    if (matrix.get(x, y)) {
                        g.fillRect(x, y, 1, 1);
                    }
                }
            }

            g.dispose();
            return image;
        } catch (WriterException e) {
            BufferedImage fallback = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = fallback.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, size, size);
            g.setColor(Color.RED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            String msg = "QR Code Error";
            FontMetrics fm = g.getFontMetrics();
            int x = (size - fm.stringWidth(msg)) / 2;
            int y = size / 2;
            g.drawString(msg, x, y);
            g.dispose();
            return fallback;
        }
    }
}
