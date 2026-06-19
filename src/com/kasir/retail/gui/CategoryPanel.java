package com.kasir.retail.gui;

import com.kasir.retail.model.Category;
import com.kasir.retail.service.KasirService;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class CategoryPanel extends JPanel {
    private final KasirService service;
    private JTable table;
    private DefaultTableModel model;

    public CategoryPanel(KasirService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBorder(ThemeManager.PADDING);
        setBackground(ThemeManager.BG);

        add(createToolbar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        refresh();
    }

    private JPanel createToolbar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel title = new JLabel("Manajemen Kategori");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.TEXT);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 5));
        btnPanel.setBackground(ThemeManager.CARD_BG);

        JButton btnAdd = ThemeManager.createButton("Tambah", ThemeManager.SUCCESS);
        btnAdd.addActionListener(e -> addCategory());
        btnPanel.add(btnAdd);

        JButton btnEdit = ThemeManager.createButton("Edit", ThemeManager.INFO);
        btnEdit.addActionListener(e -> editCategory());
        btnPanel.add(btnEdit);

        JButton btnDelete = ThemeManager.createButton("Hapus", ThemeManager.DANGER);
        btnDelete.addActionListener(e -> deleteCategory());
        btnPanel.add(btnDelete);

        JButton btnRefresh = ThemeManager.createButton("Refresh", ThemeManager.TEXT_SUBTLE);
        btnRefresh.addActionListener(e -> refresh());
        btnPanel.add(btnRefresh);

        panel.add(title, BorderLayout.NORTH);
        panel.add(btnPanel, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] cols = {"ID", "Nama Kategori"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        ThemeManager.styleTable(table);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);

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
}
