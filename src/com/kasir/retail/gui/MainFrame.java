package com.kasir.retail.gui;

import com.kasir.retail.model.User;
import com.kasir.retail.service.KasirService;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final User currentUser;

    private TransactionPanel transactionPanel;
    private MasterPanel masterPanel;
    private ReportPanel reportPanel;
    private UserPanel userPanel;

    private JPanel sidebar;
    private String currentTabName = "Transaksi";

    public MainFrame(User user) {
        this(user, new KasirService());
    }

    public MainFrame(User user, KasirService service) {
        this.currentUser = user;
        setTitle("Kasir Retail - " + user.getFullName() + " (" + user.getRole() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));

        try {
            java.net.URL iconUrl = getClass().getResource("/com/kasir/retail/gui/icon.png");
            if (iconUrl != null) {
                setIconImage(new ImageIcon(iconUrl).getImage());
            }
        } catch (Exception ignored) { }

        transactionPanel = new TransactionPanel(service);
        transactionPanel.setParentFrame(this);
        masterPanel = new MasterPanel(service);
        reportPanel = new ReportPanel(service);

        contentPanel.setBackground(ThemeManager.BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.add(transactionPanel, "Transaksi");
        contentPanel.add(masterPanel, "Produk & Kategori");
        contentPanel.add(reportPanel, "Laporan");

        if (currentUser.isAdmin()) {
            userPanel = new UserPanel(service, currentUser);
            contentPanel.add(userPanel, "Pengguna");
        }

        setLayout(new BorderLayout());
        add(createSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, "Transaksi");
        setVisible(true);
    }

    private JPanel createSidebar() {
        sidebar = new JPanel(new GridBagLayout());
        sidebar.setPreferredSize(new Dimension(ThemeManager.SIDEBAR_WIDTH, 0));
        sidebar.setBackground(ThemeManager.SIDEBAR_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                ThemeManager.paintRoundedGradient(g, 0, 0, getWidth(), getHeight(), 0,
                        ThemeManager.PRIMARY_GRADIENT_1, ThemeManager.PRIMARY_GRADIENT_2);
            }
        };
        logoPanel.setPreferredSize(new Dimension(ThemeManager.SIDEBAR_WIDTH, 120));
        logoPanel.setLayout(new GridBagLayout());
        GridBagConstraints logoGbc = new GridBagConstraints();
        logoGbc.gridx = 0;
        logoGbc.insets = new Insets(0, 0, 2, 0);

        JLabel logo = new JLabel("KASIR");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logo.setForeground(Color.WHITE);
        logoGbc.gridy = 0;
        logoPanel.add(logo, logoGbc);

        JLabel logoSub = new JLabel("RETAIL");
        logoSub.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoSub.setForeground(new Color(255, 255, 255, 200));
        logoGbc.gridy = 1;
        logoPanel.add(logoSub, logoGbc);

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        sidebar.add(logoPanel, gbc);

        JPanel userInfo = new JPanel(new GridBagLayout());
        userInfo.setBackground(ThemeManager.SIDEBAR_BG);
        userInfo.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints uGbc = new GridBagConstraints();
        uGbc.gridx = 0;
        uGbc.insets = new Insets(1, 0, 1, 0);

        JLabel avatarLabel = new JLabel("", SwingConstants.CENTER);
        avatarLabel.setPreferredSize(new Dimension(50, 50));
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(ThemeManager.PRIMARY);
        avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        String initial = currentUser.getFullName().substring(0, 1).toUpperCase();
        avatarLabel.setText(initial);
        uGbc.insets = new Insets(0, 0, 8, 0);
        userInfo.add(avatarLabel, uGbc);

        JLabel userName = new JLabel(currentUser.getFullName(), SwingConstants.CENTER);
        userName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userName.setForeground(Color.WHITE);
        uGbc.insets = new Insets(1, 0, 1, 0);
        uGbc.gridy = 1;
        userInfo.add(userName, uGbc);

        String roleDisplay = currentUser.isAdmin() ? "Administrator" : "Kasir";
        JLabel userRole = new JLabel(roleDisplay, SwingConstants.CENTER);
        userRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userRole.setForeground(ThemeManager.TEXT_MUTED);

        JPanel roleBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
        roleBadge.setBackground(ThemeManager.SIDEBAR_BG);
        JLabel dot = new JLabel("");
        dot.setPreferredSize(new Dimension(8, 8));
        dot.setOpaque(true);
        dot.setBackground(ThemeManager.SUCCESS);
        roleBadge.add(dot);
        roleBadge.add(userRole);
        uGbc.gridy = 2;
        userInfo.add(roleBadge, uGbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 5, 0);
        sidebar.add(userInfo, gbc);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(50, 54, 72));
        sep.setBackground(new Color(50, 54, 72));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 20, 5, 20);
        sidebar.add(sep, gbc);
        gbc.insets = new Insets(2, 12, 2, 12);

        int menuY = 3;

        if (currentUser.isAdmin()) {
            addMenuItem(sidebar, gbc, menuY++, "Transaksi", "Transaksi", "Transaksi".equalsIgnoreCase(currentTabName));
            addMenuItem(sidebar, gbc, menuY++, "Produk & Kategori", "Produk & Kategori", "Produk & Kategori".equalsIgnoreCase(currentTabName));
            addMenuItem(sidebar, gbc, menuY++, "Laporan", "Laporan", "Laporan".equalsIgnoreCase(currentTabName));
            addMenuItem(sidebar, gbc, menuY++, "Pengguna", "Pengguna", "Pengguna".equalsIgnoreCase(currentTabName));
        } else {
            addMenuItem(sidebar, gbc, menuY++, "Transaksi", "Transaksi", "Transaksi".equalsIgnoreCase(currentTabName));
            addMenuItem(sidebar, gbc, menuY++, "Laporan", "Laporan", "Laporan".equalsIgnoreCase(currentTabName));
        }

        gbc.gridy = menuY;
        gbc.weighty = 1;
        sidebar.add(Box.createVerticalGlue(), gbc);

        JButton btnLogout = createLogoutButton();
        gbc.gridy = menuY + 1;
        gbc.weighty = 0;
        gbc.insets = new Insets(5, 12, 15, 12);
        sidebar.add(btnLogout, gbc);

        return sidebar;
    }

    private void addMenuItem(JPanel sidebar, GridBagConstraints gbc, int y, String text, String action, boolean active) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(ThemeManager.SIDEBAR_BG);

        JPanel activeBar = new JPanel();
        activeBar.setPreferredSize(new Dimension(4, 45));
        activeBar.setBackground(active ? ThemeManager.PRIMARY : ThemeManager.SIDEBAR_BG);
        wrapper.add(activeBar, BorderLayout.WEST);

        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(active ? ThemeManager.SIDEBAR_ACTIVE : ThemeManager.SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(ThemeManager.SIDEBAR_WIDTH - 4, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 0));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (activeBar.getBackground() != ThemeManager.PRIMARY) {
                    btn.setBackground(ThemeManager.SIDEBAR_HOVER);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (activeBar.getBackground() != ThemeManager.PRIMARY) {
                    btn.setBackground(ThemeManager.SIDEBAR_BG);
                }
            }
        });

        String targetAction = action;
        btn.addActionListener(e -> {
            currentTabName = targetAction;
            setActiveMenu(wrapper);
            cardLayout.show(contentPanel, targetAction);
            if (targetAction.equals("Transaksi")) transactionPanel.refresh();
            if (targetAction.equals("Produk & Kategori")) masterPanel.refresh();
            if (targetAction.equals("Laporan")) reportPanel.refresh();
            if (targetAction.equals("Pengguna") && userPanel != null) userPanel.refresh();
        });

        wrapper.add(btn, BorderLayout.CENTER);

        gbc.gridy = y;
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.weighty = 0;
        sidebar.add(wrapper, gbc);
    }

    private void setActiveMenu(JPanel activeWrapper) {
        for (Component comp : sidebar.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel wrapper = (JPanel) comp;
                if (wrapper.getComponentCount() == 2 && wrapper.getComponent(0) instanceof JPanel) {
                    wrapper.getComponent(0).setBackground(ThemeManager.SIDEBAR_BG);
                }
                for (Component c : wrapper.getComponents()) {
                    if (c instanceof JButton) {
                        c.setBackground(ThemeManager.SIDEBAR_BG);
                    }
                }
            }
        }

        if (activeWrapper.getComponentCount() == 2 && activeWrapper.getComponent(0) instanceof JPanel) {
            activeWrapper.getComponent(0).setBackground(ThemeManager.PRIMARY);
        }
        for (Component c : activeWrapper.getComponents()) {
            if (c instanceof JButton) {
                c.setBackground(ThemeManager.SIDEBAR_ACTIVE);
            }
        }
    }

    public void setSelectedTab(String tabName) {
        this.currentTabName = tabName;
        cardLayout.show(contentPanel, tabName);

        for (Component comp : sidebar.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel wrapper = (JPanel) comp;
                for (Component c : wrapper.getComponents()) {
                    if (c instanceof JButton) {
                        JButton btn = (JButton) c;
                        if (btn.getText().trim().equalsIgnoreCase(tabName)) {
                            setActiveMenu(wrapper);
                            break;
                        }
                    }
                }
            }
        }

        if (tabName.equals("Transaksi")) transactionPanel.refresh();
        if (tabName.equals("Produk & Kategori")) masterPanel.refresh();
        if (tabName.equals("Laporan")) reportPanel.refresh();
        if (tabName.equals("Pengguna") && userPanel != null) userPanel.refresh();
    }

    private JButton createLogoutButton() {
        JButton btn = new JButton("KELUAR");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(255, 200, 200));
        btn.setBackground(ThemeManager.SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(ThemeManager.SIDEBAR_WIDTH - 24, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 100, 100, 80), 1, true),
                BorderFactory.createEmptyBorder(0, 20, 0, 0)
        ));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(50, 35, 45));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(ThemeManager.SIDEBAR_BG);
            }
        });

        btn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Yakin ingin keluar?", "Konfirmasi",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
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
        });

        return btn;
    }
}
