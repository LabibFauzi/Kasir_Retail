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

public class MasterPanel extends JPanel {
    private final KasirService service;

    private JTable productTable;
    private DefaultTableModel productModel;
    private JTable categoryTable;
    private DefaultTableModel categoryModel;

    public MasterPanel(KasirService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 0));
        setBorder(ThemeManager.PADDING);
        setBackground(ThemeManager.BG);

        add(createTopBar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                createProductSection(), createCategorySection());
        split.setResizeWeight(0.7);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(ThemeManager.BG);
        add(split, BorderLayout.CENTER);
    }

    private JPanel createTopBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        JLabel title = new JLabel("Manajemen Produk & Kategori");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.TEXT);
        panel.add(title, BorderLayout.WEST);

        return panel;
    }

    private JPanel createProductSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(ThemeManager.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBackground(ThemeManager.BG);

        JLabel lbl = new JLabel("Produk");
        lbl.setFont(ThemeManager.FONT_SUBTITLE);
        lbl.setForeground(ThemeManager.TEXT);
        header.add(lbl, BorderLayout.NORTH);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnBar.setBackground(ThemeManager.BG);

        JButton btnAdd = ThemeManager.createButton("Tambah", ThemeManager.SUCCESS);
        btnAdd.addActionListener(e -> addProduct());
        btnBar.add(btnAdd);

        JButton btnEdit = ThemeManager.createButton("Edit", ThemeManager.INFO);
        btnEdit.addActionListener(e -> editProduct());
        btnBar.add(btnEdit);

        JButton btnDelete = ThemeManager.createButton("Hapus", ThemeManager.DANGER);
        btnDelete.addActionListener(e -> deleteProduct());
        btnBar.add(btnDelete);

        header.add(btnBar, BorderLayout.CENTER);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Kode", "Nama", "Kategori", "Harga", "Stok"};
        productModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        productTable = new JTable(productModel);
        ThemeManager.styleTable(productTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        productTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        productTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        productTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(220);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        productTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        productTable.getColumnModel().getColumn(5).setPreferredWidth(60);
        productTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(productTable);
        scroll.getViewport().setBackground(ThemeManager.CARD_BG);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        scroll.getViewport().setOpaque(true);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCategorySection() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(ThemeManager.BG);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBackground(ThemeManager.BG);

        JLabel lbl = new JLabel("Kategori");
        lbl.setFont(ThemeManager.FONT_SUBTITLE);
        lbl.setForeground(ThemeManager.TEXT);
        header.add(lbl, BorderLayout.NORTH);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnBar.setBackground(ThemeManager.BG);

        JButton btnAdd = ThemeManager.createButton("Tambah", ThemeManager.SUCCESS);
        btnAdd.addActionListener(e -> addCategory());
        btnBar.add(btnAdd);

        JButton btnEdit = ThemeManager.createButton("Edit", ThemeManager.INFO);
        btnEdit.addActionListener(e -> editCategory());
        btnBar.add(btnEdit);

        JButton btnDelete = ThemeManager.createButton("Hapus", ThemeManager.DANGER);
        btnDelete.addActionListener(e -> deleteCategory());
        btnBar.add(btnDelete);

        header.add(btnBar, BorderLayout.CENTER);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Nama Kategori"};
        categoryModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        categoryTable = new JTable(categoryModel);
        ThemeManager.styleTable(categoryTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        categoryTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        categoryTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        categoryTable.getColumnModel().getColumn(1).setPreferredWidth(400);

        JScrollPane scroll = new JScrollPane(categoryTable);
        scroll.getViewport().setBackground(ThemeManager.CARD_BG);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        scroll.getViewport().setOpaque(true);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    public void refresh() {
        refreshProducts();
        refreshCategories();
    }

    private void refreshProducts() {
        try {
            List<Product> list = service.getAllProducts();
            productModel.setRowCount(0);
            for (Product p : list) {
                productModel.addRow(new Object[]{
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

    private void refreshCategories() {
        try {
            List<Category> list = service.getAllCategories();
            categoryModel.setRowCount(0);
            for (Category c : list) {
                categoryModel.addRow(new Object[]{c.getId(), c.getName()});
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

        try {
            String nextCode = service.generateNextProductCode();
            codeField.setText(nextCode);
        } catch (SQLException e) {
            // ignore
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Kode:"), gbc);

        JPanel codeRow = new JPanel(new BorderLayout(6, 0));
        codeRow.setOpaque(false);
        JButton btnScan = ThemeManager.createButton("Scan", ThemeManager.PRIMARY);
        btnScan.setPreferredSize(new Dimension(80, 30));
        btnScan.setToolTipText("Scan barcode pada kemasan produk dengan kamera");
        btnScan.addActionListener(e -> openBarcodeScanForField(codeField));
        codeRow.add(codeField, BorderLayout.CENTER);
        codeRow.add(btnScan, BorderLayout.EAST);

        gbc.gridx = 1;
        panel.add(codeRow, gbc);

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
                refreshProducts();
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
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih produk yang akan diedit!");
            return;
        }
        try {
            int id = Integer.parseInt(productModel.getValueAt(row, 0).toString());
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

            JPanel codeRow = new JPanel(new BorderLayout(6, 0));
            codeRow.setOpaque(false);
            JButton btnScan = ThemeManager.createButton("Scan", ThemeManager.PRIMARY);
            btnScan.setPreferredSize(new Dimension(80, 30));
            btnScan.setToolTipText("Scan barcode pada kemasan produk dengan kamera");
            btnScan.addActionListener(e -> openBarcodeScanForField(codeField));
            codeRow.add(codeField, BorderLayout.CENTER);
            codeRow.add(btnScan, BorderLayout.EAST);

            gbc.gridx = 1;
            panel.add(codeRow, gbc);

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
                    refreshProducts();
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
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih produk yang akan dihapus!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Yakin ingin menghapus produk ini?", "Konfirmasi",
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(productModel.getValueAt(row, 0).toString());
                service.deleteProduct(id);
                refreshProducts();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void addCategory() {
        String name = JOptionPane.showInputDialog(this, "Nama kategori:");
        if (name != null && !name.trim().isEmpty()) {
            try {
                service.addCategory(name.trim());
                refreshCategories();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void editCategory() {
        int row = categoryTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih kategori yang akan diedit!");
            return;
        }
        int id = Integer.parseInt(categoryModel.getValueAt(row, 0).toString());
        String currentName = categoryModel.getValueAt(row, 1).toString();
        String name = JOptionPane.showInputDialog(this, "Nama baru:", currentName);
        if (name != null && !name.trim().isEmpty()) {
            try {
                service.updateCategory(id, name.trim());
                refreshCategories();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void deleteCategory() {
        int row = categoryTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih kategori yang akan dihapus!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Yakin ingin menghapus kategori ini?", "Konfirmasi",
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(categoryModel.getValueAt(row, 0).toString());
                service.deleteCategory(id);
                refreshCategories();
                JOptionPane.showMessageDialog(this, "Kategori berhasil dihapus!");
            } catch (SQLException e) {
                String msg = e.getMessage().toLowerCase();
                if (msg.contains("constraint") || msg.contains("foreign key")) {
                    JOptionPane.showMessageDialog(this,
                        "Kategori tidak dapat dihapus karena masih digunakan oleh beberapa produk.\n" +
                        "Silakan ubah atau hapus produk yang terkait dengan kategori ini terlebih dahulu.",
                        "Gagal Menghapus Kategori", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                }
            }
        }
    }

    private void openBarcodeScanForField(JTextField targetField) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        BarcodeCaptureDialog dialog = new BarcodeCaptureDialog(owner, code -> {
            targetField.setText(code);
        });
        dialog.setVisible(true);
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
