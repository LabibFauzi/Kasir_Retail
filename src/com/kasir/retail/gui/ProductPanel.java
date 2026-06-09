package com.kasir.retail.gui;

import com.kasir.retail.model.Category;
import com.kasir.retail.model.Product;
import com.kasir.retail.service.KasirService;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

import static com.kasir.retail.gui.FormatUtil.*;

public class ProductPanel extends JPanel {
    private final KasirService service;
    private JTable table;
    private DefaultTableModel model;

    private final Color PRIMARY = new Color(41, 128, 185);

    public ProductPanel(KasirService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        add(createToolbar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Kelola Produk", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)));

        JButton btnAdd = new JButton("Tambah");
        styleButton(btnAdd, new Color(46, 204, 113));
        btnAdd.addActionListener(e -> addProduct());
        panel.add(btnAdd);

        JButton btnEdit = new JButton("Edit");
        styleButton(btnEdit, new Color(52, 152, 219));
        btnEdit.addActionListener(e -> editProduct());
        panel.add(btnEdit);

        JButton btnDelete = new JButton("Hapus");
        styleButton(btnDelete, new Color(231, 76, 60));
        btnDelete.addActionListener(e -> deleteProduct());
        panel.add(btnDelete);

        JButton btnRefresh = new JButton("Refresh");
        styleButton(btnRefresh, new Color(149, 165, 166));
        btnRefresh.addActionListener(e -> refresh());
        panel.add(btnRefresh);

        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] cols = {"ID", "Kode", "Nama", "Kategori", "Harga", "Stok"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(41, 128, 185, 80));

        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(50);

        return new JScrollPane(table);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(90, 30));
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

        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        panel.add(new JLabel("Kode:"));
        panel.add(codeField);
        panel.add(new JLabel("Nama:"));
        panel.add(nameField);
        panel.add(new JLabel("Kategori:"));
        panel.add(catCombo);
        panel.add(new JLabel("Harga:"));
        panel.add(priceField);
        panel.add(new JLabel("Stok:"));
        panel.add(stockField);

        int result = JOptionPane.showConfirmDialog(this, panel,
            "Tambah Produk", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String code = codeField.getText().trim().toUpperCase();
                String name = nameField.getText().trim();
                String catName = catCombo.getSelectedItem().toString();
                int catId = getCategoryId(catName);
                double price = parseIndonesianNumber(priceField.getText());
                int stock = Integer.parseInt(stockField.getText().trim());

                service.addProduct(code, name, catId, price, stock);
                refresh();
                JOptionPane.showMessageDialog(this, "Produk berhasil ditambahkan!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
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

            JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
            panel.add(new JLabel("Kode:"));
            panel.add(codeField);
            panel.add(new JLabel("Nama:"));
            panel.add(nameField);
            panel.add(new JLabel("Kategori:"));
            panel.add(catCombo);
            panel.add(new JLabel("Harga:"));
            panel.add(priceField);
            panel.add(new JLabel("Stok:"));
            panel.add(stockField);

            int result = JOptionPane.showConfirmDialog(this, panel,
                "Edit Produk", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String code = codeField.getText().trim().toUpperCase();
                String name = nameField.getText().trim();
                int catId = getCategoryId(catCombo.getSelectedItem().toString());
                double price = parseIndonesianNumber(priceField.getText());
                int stock = Integer.parseInt(stockField.getText().trim());

                service.updateProduct(id, code, name, catId, price, stock);
                refresh();
                JOptionPane.showMessageDialog(this, "Produk berhasil diupdate!");
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
