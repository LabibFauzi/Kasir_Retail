package com.kasir.retail.gui;

import com.kasir.retail.model.User;
import com.kasir.retail.service.KasirService;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel manajemen pengguna (Pengguna). Hanya tersedia untuk role ADMIN.
 * Memungkinkan Administrator membuat akun baru (Admin/Kasir), mengedit data
 * akun, mengganti password, dan menghapus akun.
 */
public class UserPanel extends JPanel {
    private final KasirService service;
    private final User currentUser;

    private JTable table;
    private DefaultTableModel model;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_KASIR = "KASIR";

    public UserPanel(KasirService service, User currentUser) {
        this.service = service;
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));
        setBorder(ThemeManager.PADDING);
        setBackground(ThemeManager.BG);

        add(createToolbar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel title = new JLabel("Manajemen Pengguna");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.TEXT);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 5));
        btnPanel.setBackground(ThemeManager.CARD_BG);

        JButton btnAdd = ThemeManager.createButton("Tambah", ThemeManager.SUCCESS);
        btnAdd.addActionListener(e -> addUser());
        btnPanel.add(btnAdd);

        JButton btnEdit = ThemeManager.createButton("Edit", ThemeManager.INFO);
        btnEdit.addActionListener(e -> editUser());
        btnPanel.add(btnEdit);

        JButton btnPassword = ThemeManager.createButton("Ganti Password", ThemeManager.PRIMARY);
        btnPassword.setPreferredSize(new Dimension(150, 38));
        btnPassword.addActionListener(e -> changePassword());
        btnPanel.add(btnPassword);

        JButton btnDelete = ThemeManager.createButton("Hapus", ThemeManager.DANGER);
        btnDelete.addActionListener(e -> deleteUser());
        btnPanel.add(btnDelete);

        panel.add(title, BorderLayout.NORTH);
        panel.add(btnPanel, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] cols = {"ID", "Nama Lengkap", "Username", "Akses"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        ThemeManager.styleTable(table);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(ThemeManager.CARD_BG);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        refresh();
        return scroll;
    }

    public void refresh() {
        try {
            List<User> list = service.getAllUsers();
            model.setRowCount(0);
            for (User u : list) {
                model.addRow(new Object[]{
                    u.getId(), u.getFullName(), u.getUsername(),
                    u.isAdmin() ? "Admin" : "Kasir"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void addUser() {
        JTextField nameField = new JTextField(20);
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Kasir", "Admin"});

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nama:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Akses:"), gbc);
        gbc.gridx = 1;
        panel.add(roleCombo, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel,
            "Tambah Pengguna", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();

                if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    throw new Exception("Nama, Username, dan Password wajib diisi!");
                }

                String role = "Admin".equals(roleCombo.getSelectedItem()) ? ROLE_ADMIN : ROLE_KASIR;
                service.addUser(username, password, role, name);
                refresh();
                JOptionPane.showMessageDialog(this, "Pengguna berhasil ditambahkan!");
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg != null && msg.toLowerCase().contains("unique constraint failed")) {
                    msg = "Username '" + usernameField.getText().trim() + "' sudah terdaftar!";
                }
                JOptionPane.showMessageDialog(this, "Error: " + msg, "Gagal Menambahkan Pengguna", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih pengguna yang akan diedit!");
            return;
        }
        try {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            User u = service.getUserById(id);
            if (u == null) return;

            JTextField nameField = new JTextField(u.getFullName());
            JTextField usernameField = new JTextField(u.getUsername());
            JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Kasir", "Admin"});
            roleCombo.setSelectedItem(u.isAdmin() ? "Admin" : "Kasir");

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("Nama:"), gbc);
            gbc.gridx = 1;
            panel.add(nameField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("Username:"), gbc);
            gbc.gridx = 1;
            panel.add(usernameField, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(new JLabel("Akses:"), gbc);
            gbc.gridx = 1;
            panel.add(roleCombo, gbc);

            int result = JOptionPane.showConfirmDialog(this, panel,
                "Edit Pengguna", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String name = nameField.getText().trim();
                    String username = usernameField.getText().trim();
                    if (name.isEmpty() || username.isEmpty()) {
                        throw new Exception("Nama dan Username wajib diisi!");
                    }

                    String role = "Admin".equals(roleCombo.getSelectedItem()) ? ROLE_ADMIN : ROLE_KASIR;

                    if (id == currentUser.getId() && ROLE_KASIR.equals(role)) {
                        throw new Exception("Tidak dapat mengubah akses akun Anda sendiri menjadi Kasir!");
                    }

                    service.updateUser(id, username, role, name);
                    refresh();
                    JOptionPane.showMessageDialog(this, "Pengguna berhasil diupdate!");
                } catch (Exception ex) {
                    String msg = ex.getMessage();
                    if (msg != null && msg.toLowerCase().contains("unique constraint failed")) {
                        msg = "Username '" + usernameField.getText().trim() + "' sudah terdaftar untuk pengguna lain!";
                    }
                    JOptionPane.showMessageDialog(this, "Error: " + msg, "Gagal Mengedit Pengguna", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void changePassword() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih pengguna yang akan diganti password-nya!");
            return;
        }
        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        String name = model.getValueAt(row, 1).toString();

        JPasswordField newPasswordField = new JPasswordField(15);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Password baru untuk " + name + ":"), gbc);
        gbc.gridy = 1;
        panel.add(newPasswordField, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel,
            "Ganti Password", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String newPassword = new String(newPasswordField.getPassword()).trim();
            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password tidak boleh kosong!");
                return;
            }
            try {
                service.changeUserPassword(id, newPassword);
                JOptionPane.showMessageDialog(this, "Password berhasil diubah!");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void deleteUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih pengguna yang akan dihapus!");
            return;
        }
        int id = Integer.parseInt(model.getValueAt(row, 0).toString());

        if (id == currentUser.getId()) {
            JOptionPane.showMessageDialog(this, "Tidak dapat menghapus akun Anda sendiri yang sedang login!",
                "Tidak Diizinkan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = model.getValueAt(row, 1).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Yakin ingin menghapus pengguna \"" + name + "\"?", "Konfirmasi",
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.deleteUser(id);
                refresh();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
}
