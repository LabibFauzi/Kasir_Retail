package com.kasir.retail.gui;

import com.kasir.retail.database.DatabaseManager;
import com.kasir.retail.service.KasirService;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final KasirService service = new KasirService();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private TransactionPanel transactionPanel;
    private ProductPanel productPanel;
    private CategoryPanel categoryPanel;
    private ReportPanel reportPanel;

    private final Color PRIMARY = new Color(41, 128, 185);
    private final Color DARK = new Color(44, 62, 80);
    private final Color HOVER = new Color(52, 152, 219);
    private final Color BG = new Color(236, 240, 241);

    public MainFrame() {
        setTitle("Kasir Retail");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        try {
            java.net.URL iconUrl = getClass().getResource("/com/kasir/retail/gui/icon.png");
            if (iconUrl != null) {
                setIconImage(new ImageIcon(iconUrl).getImage());
            }
        } catch (Exception ignored) { }

        transactionPanel = new TransactionPanel(service);
        productPanel = new ProductPanel(service);
        categoryPanel = new CategoryPanel(service);
        reportPanel = new ReportPanel(service);

        contentPanel.add(transactionPanel, "Transaksi");
        contentPanel.add(productPanel, "Produk");
        contentPanel.add(categoryPanel, "Kategori");
        contentPanel.add(reportPanel, "Laporan");

        setLayout(new BorderLayout());
        add(createSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, "Transaksi");
        setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new GridBagLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 10, 2, 10);
        gbc.gridx = 0;

        JLabel logo = new JLabel("KASIR RETAIL", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);
        logo.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        gbc.gridy = 0;
        sidebar.add(logo, gbc);

        String[][] menuItems = {
            {"Transaksi", "Transaksi"},
            {"Produk", "Produk"},
            {"Kategori", "Kategori"},
            {"Laporan", "Laporan"}
        };

        for (int i = 0; i < menuItems.length; i++) {
            JButton btn = createMenuButton(menuItems[i][0], menuItems[i][1]);
            gbc.gridy = i + 1;
            gbc.weighty = 0;
            sidebar.add(btn, gbc);
        }

        gbc.gridy = menuItems.length + 1;
        gbc.weighty = 1;
        sidebar.add(Box.createVerticalGlue(), gbc);

        JButton btnExit = createMenuButton("Keluar", "exit");
        gbc.gridy = menuItems.length + 2;
        gbc.weighty = 0;
        sidebar.add(btnExit, gbc);

        return sidebar;
    }

    private JButton createMenuButton(String text, String panelName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(DARK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(200, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!btn.getText().equals("Keluar")) {
                    btn.setBackground(HOVER);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(DARK);
            }
        });

        if (panelName.equals("exit")) {
            btn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Yakin ingin keluar?", "Konfirmasi",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            });
        } else {
            btn.addActionListener(e -> {
                cardLayout.show(contentPanel, panelName);
                if (panelName.equals("Produk")) productPanel.refresh();
                if (panelName.equals("Kategori")) categoryPanel.refresh();
                if (panelName.equals("Laporan")) reportPanel.refresh();
            });
        }

        return btn;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }
        DatabaseManager.initializeDatabase();
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
