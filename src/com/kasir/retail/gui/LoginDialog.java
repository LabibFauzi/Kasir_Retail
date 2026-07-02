package com.kasir.retail.gui;

import com.kasir.retail.dao.UserDAO;
import com.kasir.retail.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginDialog extends JDialog {
    private final UserDAO userDAO = new UserDAO();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private User loggedInUser;

    public LoginDialog(JFrame parent) {
        super(parent, "Login - Kasir Retail", true);
        setSize(420, 520);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        try {
            setIconImage(new ImageIcon(getClass().getResource("/com/kasir/retail/gui/icon.png")).getImage());
        } catch (Exception ignored) {}

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ThemeManager.BG);

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createFormPanel(), BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, ThemeManager.PRIMARY_GRADIENT_1,
                        getWidth(), getHeight(), ThemeManager.PRIMARY_GRADIENT_2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillOval(-80, -40, 250, 200);
                g2.fillOval(280, -20, 180, 150);
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillOval(150, 60, 200, 120);
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(420, 140));
        panel.setLayout(new GridBagLayout());

        JLabel iconLabel = new JLabel("KASIR");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        iconLabel.setForeground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(iconLabel, gbc);

        JLabel subLabel = new JLabel("RETAIL");
        subLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        subLabel.setForeground(new Color(255, 255, 255, 200));
        gbc.gridy = 1;
        panel.add(subLabel, gbc);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeManager.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridx = 0;

        JLabel titleLabel = new JLabel("Masuk ke Akun");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ThemeManager.TEXT);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(titleLabel, gbc);

        gbc.insets = new Insets(3, 0, 3, 0);

        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(ThemeManager.TEXT_SUBTLE);
        gbc.gridy = 1;
        panel.add(lblUser, gbc);

        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        gbc.gridy = 2;
        panel.add(usernameField, gbc);

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(ThemeManager.TEXT_SUBTLE);
        gbc.gridy = 3;
        panel.add(lblPass, gbc);

        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        gbc.gridy = 4;
        panel.add(passwordField, gbc);

        passwordField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogin();
                }
            }
        });
        usernameField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doLogin();
                }
            }
        });

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(ThemeManager.DANGER);
        gbc.gridy = 5;
        panel.add(statusLabel, gbc);

        loginButton = new JButton("MASUK") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                if (getModel().isPressed()) {
                    g2.setColor(ThemeManager.PRIMARY_DARK);
                } else if (getModel().isRollover()) {
                    g2.setColor(ThemeManager.PRIMARY_LIGHT);
                } else {
                    g2.setPaint(new GradientPaint(0, 0, ThemeManager.PRIMARY_GRADIENT_1,
                            getWidth(), getHeight(), ThemeManager.PRIMARY_GRADIENT_2));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setPreferredSize(new Dimension(0, 45));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> doLogin());
        loginButton.putClientProperty("JButton.roundType", "round");

        gbc.insets = new Insets(18, 0, 5, 0);
        gbc.gridy = 6;
        panel.add(loginButton, gbc);

        return panel;
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username dan password harus diisi!");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Memproses...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                Thread.sleep(500);
                return userDAO.authenticate(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        loggedInUser = user;
                        dispose();
                    } else {
                        statusLabel.setText("Username atau password salah!");
                        loginButton.setEnabled(true);
                        loginButton.setText("MASUK");
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    loginButton.setEnabled(true);
                    loginButton.setText("MASUK");
                }
            }
        };
        worker.execute();
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}
