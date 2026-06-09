package com.kasir.retail.gui;

import com.kasir.retail.model.Category;
import com.kasir.retail.service.KasirService;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class CategoryPanel extends JPanel {
    private final KasirService service;
    private JTable table;
    private DefaultTableModel model;

    private final Color PRIMARY = new Color(41, 128, 185);

    public CategoryPanel(KasirService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        add(createToolbar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        refresh();
    }

    private JPanel createToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Kelola Kategori", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)));

        JButton btnAdd = new JButton("Tambah");
        styleButton(btnAdd, new Color(46, 204, 113));
        btnAdd.addActionListener(e -> addCategory());
        panel.add(btnAdd);

        JButton btnEdit = new JButton("Edit");
        styleButton(btnEdit, new Color(52, 152, 219));
        btnEdit.addActionListener(e -> editCategory());
        panel.add(btnEdit);

        JButton btnDelete = new JButton("Hapus");
        styleButton(btnDelete, new Color(231, 76, 60));
        btnDelete.addActionListener(e -> deleteCategory());
        panel.add(btnDelete);

        JButton btnRefresh = new JButton("Refresh");
        styleButton(btnRefresh, new Color(149, 165, 166));
        btnRefresh.addActionListener(e -> refresh());
        panel.add(btnRefresh);

        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] cols = {"ID", "Nama Kategori"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(41, 128, 185, 80));

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);

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
            List<Category> list = service.getAllCategories();
            model.setRowCount(0);
            for (Category c : list) {
                model.addRow(new Object[]{c.getId(), c.getName()});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void addCategory() {
        String name = JOptionPane.showInputDialog(this, "Nama kategori:");
        if (name != null && !name.trim().isEmpty()) {
            try {
                service.addCategory(name.trim());
                refresh();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void editCategory() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih kategori yang akan diedit!");
            return;
        }
        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        String currentName = model.getValueAt(row, 1).toString();
        String name = JOptionPane.showInputDialog(this, "Nama baru:", currentName);
        if (name != null && !name.trim().isEmpty()) {
            try {
                service.updateCategory(id, name.trim());
                refresh();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void deleteCategory() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih kategori yang akan dihapus!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Yakin ingin menghapus kategori ini?", "Konfirmasi",
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(model.getValueAt(row, 0).toString());
                service.deleteCategory(id);
                refresh();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
}
