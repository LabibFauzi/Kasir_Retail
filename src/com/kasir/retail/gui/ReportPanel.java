package com.kasir.retail.gui;

import com.kasir.retail.model.Transaction;
import com.kasir.retail.model.TransactionItem;
import com.kasir.retail.service.KasirService;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ReportPanel extends JPanel {
    private final KasirService service;
    private JTable table;
    private DefaultTableModel model;
    private JLabel dailyTotalLabel;
    private JTextArea detailArea;

    private final Color PRIMARY = new Color(41, 128, 185);

    public ReportPanel(KasirService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Ringkasan", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)));

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        infoPanel.setBackground(Color.WHITE);

        dailyTotalLabel = new JLabel("Rp0");
        dailyTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        dailyTotalLabel.setForeground(new Color(231, 76, 60));

        JPanel dailyPanel = new JPanel(new BorderLayout());
        dailyPanel.setBackground(Color.WHITE);
        JLabel lblDaily = new JLabel("Total Penjualan Hari Ini");
        lblDaily.setForeground(Color.BLACK);
        dailyPanel.add(lblDaily, BorderLayout.NORTH);
        dailyPanel.add(dailyTotalLabel, BorderLayout.CENTER);
        infoPanel.add(dailyPanel);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(PRIMARY);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> refresh());
        infoPanel.add(btnRefresh);

        JButton btnDetail = new JButton("Lihat Detail");
        btnDetail.setBackground(new Color(46, 204, 113));
        btnDetail.setFocusPainted(false);
        btnDetail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDetail.addActionListener(e -> showDetail());
        infoPanel.add(btnDetail);

        panel.add(infoPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCenterPanel() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.5);
        split.setDividerLocation(550);

        String[] cols = {"ID", "Invoice", "Tanggal", "Total", "Bayar", "Kembali"};
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
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Riwayat Transaksi", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13)));
        split.setLeftComponent(scrollTable);

        detailArea = new JTextArea();
        detailArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        detailArea.setEditable(false);
        detailArea.setBackground(Color.WHITE);
        detailArea.setForeground(Color.BLACK);
        detailArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollDetail = new JScrollPane(detailArea);
        scrollDetail.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Detail Transaksi", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13)));
        split.setRightComponent(scrollDetail);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showDetail();
            }
        });

        JPanel center = new JPanel(new BorderLayout());
        center.add(split, BorderLayout.CENTER);
        return center;
    }

    public void refresh() {
        try {
            double dailyTotal = service.getTotalSalesToday();
            dailyTotalLabel.setText(String.format("Rp%,.0f", dailyTotal));

            List<Transaction> list = service.getAllTransactions();
            model.setRowCount(0);
            for (Transaction t : list) {
                model.addRow(new Object[]{
                    t.getId(), t.getInvoice(), t.getDateFormatted(),
                    String.format("Rp%,.0f", t.getTotal()),
                    String.format("Rp%,.0f", t.getPayment()),
                    String.format("Rp%,.0f", t.getChange())
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void showDetail() {
        int row = table.getSelectedRow();
        if (row < 0) {
            detailArea.setText("Pilih transaksi untuk melihat detail");
            return;
        }
        try {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            Transaction t = service.getTransactionDetail(id);
            if (t == null) return;

            StringBuilder sb = new StringBuilder();
            sb.append("========================================\n");
            sb.append("           DETAIL TRANSAKSI\n");
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
            sb.append("========================================");

            detailArea.setText(sb.toString());
        } catch (SQLException e) {
            detailArea.setText("Error: " + e.getMessage());
        }
    }
}
