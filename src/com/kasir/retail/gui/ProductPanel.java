package com.kasir.retail.gui;

import com.kasir.retail.model.Category;
import com.kasir.retail.model.Product;
import com.kasir.retail.service.KasirService;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

import static com.kasir.retail.gui.FormatUtil.*;

public class ProductPanel extends JPanel {
    private final KasirService service;
    private JTable table;
    private DefaultTableModel model;

    public ProductPanel(KasirService service) {
        this.service = service;
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

        JLabel title = new JLabel("Manajemen Produk");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.TEXT);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 5));
        btnPanel.setBackground(ThemeManager.CARD_BG);

        JButton btnAdd = ThemeManager.createButton("Tambah", ThemeManager.SUCCESS);
        btnAdd.addActionListener(e -> addProduct());
        btnPanel.add(btnAdd);

        JButton btnEdit = ThemeManager.createButton("Edit", ThemeManager.INFO);
        btnEdit.addActionListener(e -> editProduct());
        btnPanel.add(btnEdit);

        JButton btnDelete = ThemeManager.createButton("Hapus", ThemeManager.DANGER);
        btnDelete.addActionListener(e -> deleteProduct());
        btnPanel.add(btnDelete);

        panel.add(title, BorderLayout.NORTH);
        panel.add(btnPanel, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] cols = {"ID", "Kode", "Nama", "Kategori", "Harga", "Stok"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        ThemeManager.styleTable(table);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(5).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(ThemeManager.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(ThemeManager.CARD_BG);
        card.add(scroll, BorderLayout.CENTER);

        return scroll;
    }

    public void refresh() {
        try {
            List<Product> list = service.getAllProducts();
            model.setRowCount(0);
            for (Product p : list) {
                model.addRow(new Object[]{
                    p.getId(), p.getCode(), p.getName(),
                    p.getCategoryName(),
                    String.format("Rp%,.0f", p.getPrice()),
                    p.getStock()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void addProduct() {
        JTextField codeField = new JTextField(10);
        JTextField nameField = new JTextField(20);
        JComboBox<String> catCombo = new JComboBox<>();
        JTextField priceField = new JTextField(10);
        JTextField stockField = new JTextField(5);

        loadCategories(catCombo);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Kode:"), gbc);
        gbc.gridx = 1;
        panel.add(codeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Nama:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Kategori:"), gbc);
        gbc.gridx = 1;
        panel.add(catCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Harga:"), gbc);
        gbc.gridx = 1;
        panel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Stok:"), gbc);
        gbc.gridx = 1;
        panel.add(stockField, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel,
            "Tambah Produk", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String code = codeField.getText().trim().toUpperCase();
                String name = nameField.getText().trim();
                if (code.isEmpty() || name.isEmpty()) {
                    throw new Exception("Kode dan Nama produk wajib diisi!");
                }
                if (catCombo.getSelectedItem() == null) {
                    throw new Exception("Pilih kategori produk!");
                }
                String catName = catCombo.getSelectedItem().toString();
                int catId = getCategoryId(catName);

                double price;
                try {
                    price = parseIndonesianNumber(priceField.getText());
                } catch (NumberFormatException e) {
                    throw new Exception("Format harga tidak valid! Harap isi dengan angka.");
                }
                if (price < 0) {
                    throw new Exception("Harga produk tidak boleh negatif!");
                }

                int stock;
                try {
                    stock = Integer.parseInt(stockField.getText().trim());
                } catch (NumberFormatException e) {
                    throw new Exception("Format stok tidak valid! Harap isi dengan angka bulat.");
                }
                if (stock < 0) {
                    throw new Exception("Stok produk tidak boleh negatif!");
                }

                service.addProduct(code, name, catId, price, stock);
                refresh();
                JOptionPane.showMessageDialog(this, "Produk berhasil ditambahkan!");
            } catch (Exception ex) {
                String msg = ex.getMessage();
                if (msg != null && msg.toLowerCase().contains("unique constraint failed")) {
                    msg = "Kode produk '" + codeField.getText().trim().toUpperCase() + "' sudah terdaftar untuk produk lain!";
                }
                JOptionPane.showMessageDialog(this, "Error: " + msg, "Gagal Menambahkan Produk", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editProduct() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih produk yang akan diedit!");
            return;
        }
        try {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            Product p = service.getProductById(id);
            if (p == null) return;

            JTextField codeField = new JTextField(p.getCode());
            JTextField nameField = new JTextField(p.getName());
            JComboBox<String> catCombo = new JComboBox<>();
            JTextField priceField = new JTextField(String.valueOf((int) p.getPrice()));
            JTextField stockField = new JTextField(String.valueOf(p.getStock()));

            loadCategories(catCombo);
            catCombo.setSelectedItem(p.getCategoryName());

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("Kode:"), gbc);
            gbc.gridx = 1;
            panel.add(codeField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("Nama:"), gbc);
            gbc.gridx = 1;
            panel.add(nameField, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(new JLabel("Kategori:"), gbc);
            gbc.gridx = 1;
            panel.add(catCombo, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            panel.add(new JLabel("Harga:"), gbc);
            gbc.gridx = 1;
            panel.add(priceField, gbc);

            gbc.gridx = 0; gbc.gridy = 4;
            panel.add(new JLabel("Stok:"), gbc);
            gbc.gridx = 1;
            panel.add(stockField, gbc);

            int result = JOptionPane.showConfirmDialog(this, panel,
                "Edit Produk", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String code = codeField.getText().trim().toUpperCase();
                    String name = nameField.getText().trim();
                    if (code.isEmpty() || name.isEmpty()) {
                        throw new Exception("Kode dan Nama produk wajib diisi!");
                    }
                    if (catCombo.getSelectedItem() == null) {
                        throw new Exception("Pilih kategori produk!");
                    }
                    int catId = getCategoryId(catCombo.getSelectedItem().toString());

                    double price;
                    try {
                        price = parseIndonesianNumber(priceField.getText());
                    } catch (NumberFormatException e) {
                        throw new Exception("Format harga tidak valid! Harap isi dengan angka.");
                    }
                    if (price < 0) {
                        throw new Exception("Harga produk tidak boleh negatif!");
                    }

                    int stock;
                    try {
                        stock = Integer.parseInt(stockField.getText().trim());
                    } catch (NumberFormatException e) {
                        throw new Exception("Format stok tidak valid! Harap isi dengan angka bulat.");
                    }
                    if (stock < 0) {
                        throw new Exception("Stok produk tidak boleh negatif!");
                    }

                    service.updateProduct(id, code, name, catId, price, stock);
                    refresh();
                    JOptionPane.showMessageDialog(this, "Produk berhasil diupdate!");
                } catch (Exception ex) {
                    String msg = ex.getMessage();
                    if (msg != null && msg.toLowerCase().contains("unique constraint failed")) {
                        msg = "Kode produk '" + codeField.getText().trim().toUpperCase() + "' sudah terdaftar untuk produk lain!";
                    }
                    JOptionPane.showMessageDialog(this, "Error: " + msg, "Gagal Mengedit Produk", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void deleteProduct() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih produk yang akan dihapus!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Yakin ingin menghapus produk ini?", "Konfirmasi",
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(model.getValueAt(row, 0).toString());
                service.deleteProduct(id);
                refresh();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void loadCategories(JComboBox<String> combo) {
        try {
            List<Category> list = service.getAllCategories();
            for (Category c : list) {
                combo.addItem(c.getName());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private int getCategoryId(String name) {
        try {
            for (Category c : service.getAllCategories()) {
                if (c.getName().equals(name)) return c.getId();
            }
        } catch (SQLException e) { }
        return 0;
    }
}
