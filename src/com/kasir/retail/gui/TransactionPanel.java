package com.kasir.retail.gui;

import com.kasir.retail.model.*;
import com.kasir.retail.service.CartItem;
import com.kasir.retail.service.KasirService;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

import static com.kasir.retail.gui.FormatUtil.*;

public class TransactionPanel extends JPanel {
    private final KasirService service;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;
    private JTextField codeField, qtyField, searchField;
    private JTable productTable;
    private DefaultTableModel productModel;
    private JTextArea receiptArea;

    private final Color HEADER_BG = new Color(41, 128, 185);
    private final Color TOTAL_BG = new Color(231, 76, 60);

    public TransactionPanel(KasirService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.6);
        split.setDividerLocation(700);

        split.setLeftComponent(createLeftPanel());
        split.setRightComponent(createRightPanel());

        add(split, BorderLayout.CENTER);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);

        panel.add(createSearchPanel(), BorderLayout.NORTH);
        panel.add(createProductScroll(), BorderLayout.CENTER);
        panel.add(createCartPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBackground(Color.WHITE);

        JLabel lblCari = new JLabel("Cari Produk:");
        lblCari.setForeground(Color.BLACK);
        panel.add(lblCari);
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(searchField);

        JButton btnSearch = new JButton("Cari");
        btnSearch.setBackground(HEADER_BG);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> searchProduct());
        panel.add(btnSearch);

        JButton btnAll = new JButton("Semua");
        btnAll.setBackground(new Color(52, 73, 94));
        btnAll.setFocusPainted(false);
        btnAll.addActionListener(e -> loadProducts());
        panel.add(btnAll);

        searchField.addActionListener(e -> searchProduct());
        return panel;
    }

    private JScrollPane createProductScroll() {
        String[] cols = {"Kode", "Nama", "Kategori", "Harga", "Stok"};
        productModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        productTable = new JTable(productModel);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        productTable.setRowHeight(28);
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        productTable.getTableHeader().setBackground(HEADER_BG);
        productTable.getTableHeader().setForeground(Color.WHITE);
        productTable.setSelectionBackground(new Color(41, 128, 185, 80));
        ((DefaultTableCellRenderer) productTable.getTableHeader().getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.CENTER);


        productTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(60);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Daftar Produk", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13)));

        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setPreferredSize(new Dimension(0, 250));
        centerPanel.add(scroll, BorderLayout.CENTER);

        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = productTable.getSelectedRow();
                    if (row >= 0) {
                        String code = productModel.getValueAt(row, 0).toString();
                        codeField.setText(code);
                        qtyField.setText("1");
                        addToCart();
                    }
                }
            }
        });

        loadProducts();
        return new JScrollPane(centerPanel);
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Keranjang Belanja", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13)));

        String[] cols = {"No", "Nama", "Harga", "Qty", "Subtotal"};
        cartModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cartTable.setRowHeight(25);
        cartTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(80);


        JScrollPane scrollCart = new JScrollPane(cartTable);
        scrollCart.setPreferredSize(new Dimension(0, 150));
        panel.add(scrollCart, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.setBackground(Color.WHITE);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        inputPanel.setBackground(Color.WHITE);
        JLabel lblKode = new JLabel("Kode:");
        lblKode.setForeground(Color.BLACK);
        inputPanel.add(lblKode);
        codeField = new JTextField(8);
        codeField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inputPanel.add(codeField);
        JLabel lblQty = new JLabel("Qty:");
        lblQty.setForeground(Color.BLACK);
        inputPanel.add(lblQty);
        qtyField = new JTextField(3);
        qtyField.setText("1");
        qtyField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inputPanel.add(qtyField);

        JButton btnAdd = new JButton("Tambah");
        btnAdd.setBackground(new Color(46, 204, 113));
        btnAdd.setFocusPainted(false);
        btnAdd.addActionListener(e -> addToCart());
        inputPanel.add(btnAdd);

        JButton btnRemove = new JButton("Hapus");
        btnRemove.setBackground(new Color(231, 76, 60));
        btnRemove.setFocusPainted(false);
        btnRemove.addActionListener(e -> removeFromCart());
        inputPanel.add(btnRemove);

        JButton btnClear = new JButton("Kosongkan");
        btnClear.setBackground(new Color(149, 165, 166));
        btnClear.setFocusPainted(false);
        btnClear.addActionListener(e -> clearCart());
        inputPanel.add(btnClear);

        bottom.add(inputPanel, BorderLayout.WEST);

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        totalPanel.setBackground(Color.WHITE);
        totalLabel = new JLabel("Rp0");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalLabel.setForeground(TOTAL_BG);
        JLabel lblTotal = new JLabel("Total: ");
        lblTotal.setForeground(Color.BLACK);
        totalPanel.add(lblTotal);
        totalPanel.add(totalLabel);

        JButton btnPay = new JButton("BAYAR");
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPay.setBackground(new Color(46, 204, 113));
        btnPay.setPreferredSize(new Dimension(120, 35));
        btnPay.setFocusPainted(false);
        btnPay.addActionListener(e -> checkout());
        totalPanel.add(btnPay);

        bottom.add(totalPanel, BorderLayout.EAST);

        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Struk", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13)));

        receiptArea = new JTextArea();
        receiptArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        receiptArea.setEditable(false);
        receiptArea.setBackground(Color.WHITE);
        receiptArea.setForeground(Color.BLACK);
        receiptArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(receiptArea);
        panel.add(scroll, BorderLayout.CENTER);

        JLabel placeholder = new JLabel("Silakan lakukan transaksi\nStruk akan tampil di sini", SwingConstants.CENTER);
        placeholder.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        placeholder.setForeground(Color.GRAY);
        receiptArea.setLayout(new BorderLayout());
        receiptArea.add(placeholder);

        return panel;
    }

    private void loadProducts() {
        try {
            List<Product> list = service.getAllProducts();
            productModel.setRowCount(0);
            for (Product p : list) {
                productModel.addRow(new Object[]{
                    p.getCode(), p.getName(), p.getCategoryName(),
                    String.format("Rp%,.0f", p.getPrice()), p.getStock()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void searchProduct() {
        String keyword = searchField.getText().trim();
        try {
            List<Product> list = service.searchProducts(keyword);
            productModel.setRowCount(0);
            for (Product p : list) {
                productModel.addRow(new Object[]{
                    p.getCode(), p.getName(), p.getCategoryName(),
                    String.format("Rp%,.0f", p.getPrice()), p.getStock()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        int i = 1;
        for (CartItem item : service.getCart()) {
            cartModel.addRow(new Object[]{
                i++, item.getProduct().getName(),
                String.format("Rp%,.0f", item.getProduct().getPrice()),
                item.getQuantity(),
                String.format("Rp%,.0f", item.getSubtotal())
            });
        }
        totalLabel.setText(String.format("Rp%,.0f", service.getCartTotal()));
    }

    private void addToCart() {
        String code = codeField.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan kode produk!");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah tidak valid!");
            return;
        }
        try {
            service.addToCart(code, qty);
            refreshCart();
            codeField.setText("");
            qtyField.setText("1");
            codeField.requestFocus();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih item di keranjang!");
            return;
        }
        service.removeFromCart(row);
        refreshCart();
    }

    private void clearCart() {
        if (service.isCartEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Kosongkan keranjang?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.clearCart();
            refreshCart();
        }
    }

    private void checkout() {
        if (service.isCartEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang kosong!");
            return;
        }
        String input = JOptionPane.showInputDialog(this,
            "Total: Rp" + String.format("%,.0f", service.getCartTotal()) +
            "\nMasukkan jumlah pembayaran:", "Pembayaran", JOptionPane.PLAIN_MESSAGE);
        if (input == null) return;
        try {
            double payment = parseIndonesianNumber(input);
            Transaction t = service.checkout(payment);
            showReceipt(t);
            refreshCart();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah tidak valid! Gunakan format: 30000 atau 30.000");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void showReceipt(Transaction t) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("           KASIR RETAIL\n");
        sb.append("========================================\n");
        sb.append(String.format("Invoice : %s\n", t.getInvoice()));
        sb.append(String.format("Tanggal : %s\n", t.getDateFormatted()));
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-18s %3s %8s\n", "Produk", "Qty", "Subtotal"));
        sb.append("----------------------------------------\n");
        for (TransactionItem item : t.getItems()) {
            sb.append(String.format("%-18s %3d Rp%,7.0f\n",
                item.getProductName(), item.getQuantity(), item.getSubtotal()));
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-22s Rp%,8.0f\n", "TOTAL", t.getTotal()));
        sb.append(String.format("%-22s Rp%,8.0f\n", "BAYAR", t.getPayment()));
        sb.append(String.format("%-22s Rp%,8.0f\n", "KEMBALI", t.getChange()));
        sb.append("========================================\n");
        sb.append("         TERIMA KASIH!\n");
        sb.append("========================================\n");

        receiptArea.setText(sb.toString());
    }
}
