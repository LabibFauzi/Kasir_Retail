package com.kasir.retail.gui;

import com.kasir.retail.model.Transaction;
import com.kasir.retail.model.TransactionItem;
import com.kasir.retail.service.KasirService;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.SQLException;
import java.util.List;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

public class ReportPanel extends JPanel {
    private final KasirService service;
    private JTable table;
    private DefaultTableModel model;
    private JLabel dailyTotalLabel;
    private JTextArea detailArea;

    public ReportPanel(KasirService service) {
        this.service = service;
        setLayout(new BorderLayout(10, 10));
        setBorder(ThemeManager.PADDING);
        setBackground(ThemeManager.BG);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(ThemeManager.BG);

        JLabel title = new JLabel("Laporan Penjualan");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(ThemeManager.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblDaily = new JLabel("Total Penjualan Hari Ini");
        lblDaily.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDaily.setForeground(ThemeManager.TEXT_SUBTLE);

        dailyTotalLabel = new JLabel("Rp0");
        dailyTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        dailyTotalLabel.setForeground(ThemeManager.PRIMARY);

        JPanel dailyPanel = new JPanel(new BorderLayout());
        dailyPanel.setBackground(ThemeManager.CARD_BG);
        dailyPanel.add(lblDaily, BorderLayout.NORTH);
        dailyPanel.add(dailyTotalLabel, BorderLayout.CENTER);

        card.add(dailyPanel, BorderLayout.WEST);

        panel.add(title, BorderLayout.NORTH);
        panel.add(card, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCenterPanel() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.5);
        split.setDividerLocation(550);
        split.setBorder(null);
        split.setBackground(ThemeManager.BG);

        String[] cols = {"ID", "Invoice", "Tanggal", "Total", "Bayar", "Kembali"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        ThemeManager.styleTable(table);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        scrollTable.getViewport().setBackground(ThemeManager.CARD_BG);

        JPanel leftCard = new JPanel(new BorderLayout());
        leftCard.setBackground(ThemeManager.CARD_BG);
        leftCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                BorderFactory.createEmptyBorder(12, 10, 10, 10)
        ));
        JLabel leftTitle = new JLabel("Riwayat Transaksi");
        leftTitle.setFont(ThemeManager.FONT_SUBTITLE);
        leftTitle.setForeground(ThemeManager.TEXT);
        leftTitle.setBorder(BorderFactory.createEmptyBorder(0, 5, 8, 0));
        leftCard.add(leftTitle, BorderLayout.NORTH);
        leftCard.add(scrollTable, BorderLayout.CENTER);
        split.setLeftComponent(leftCard);

        detailArea = new JTextArea();
        detailArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        detailArea.setEditable(false);
        detailArea.setBackground(ThemeManager.CARD_BG);
        detailArea.setForeground(ThemeManager.TEXT);
        detailArea.setMargin(new Insets(15, 15, 15, 15));
        detailArea.setCaretColor(ThemeManager.TEXT);

        JScrollPane scrollDetail = new JScrollPane(detailArea);
        scrollDetail.getViewport().setBackground(ThemeManager.CARD_BG);

        JPanel rightCard = new JPanel(new BorderLayout());
        rightCard.setBackground(ThemeManager.CARD_BG);
        rightCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                BorderFactory.createEmptyBorder(12, 10, 10, 10)
        ));

        JPanel rightHeader = new JPanel(new BorderLayout());
        rightHeader.setBackground(ThemeManager.CARD_BG);
        rightHeader.setBorder(BorderFactory.createEmptyBorder(0, 5, 8, 0));
        JLabel rightTitle = new JLabel("Detail Transaksi");
        rightTitle.setFont(ThemeManager.FONT_SUBTITLE);
        rightTitle.setForeground(ThemeManager.TEXT);
        rightHeader.add(rightTitle, BorderLayout.WEST);

        JButton btnSavePdf = ThemeManager.createButton("Cetak PDF", ThemeManager.PRIMARY);
        btnSavePdf.setPreferredSize(new Dimension(110, 30));
        btnSavePdf.addActionListener(e -> saveDetailAsPdf());
        rightHeader.add(btnSavePdf, BorderLayout.EAST);

        rightCard.add(rightHeader, BorderLayout.NORTH);
        rightCard.add(scrollDetail, BorderLayout.CENTER);
        split.setRightComponent(rightCard);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showDetail();
            }
        });

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(ThemeManager.BG);
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

    private void saveDetailAsPdf() {
        String text = detailArea.getText();
        if (text.isEmpty() || text.startsWith("Pilih transaksi")) {
            JOptionPane.showMessageDialog(this, "Tidak ada detail untuk dicetak!");
            return;
        }

        String[] lines = text.split("\n");

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Detail Transaksi");

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2.setFont(new Font("Consolas", Font.PLAIN, 9));
            FontMetrics fm = g2.getFontMetrics();
            int lineH = fm.getHeight();
            int y = 10;
            g2.setColor(Color.BLACK);
            for (String line : lines) {
                g2.drawString(line, 10, y);
                y += lineH;
            }
            return Printable.PAGE_EXISTS;
        });

        PrintService pdfService = findPDFPrinter();
        if (pdfService != null) {
            try {
                job.setPrintService(pdfService);
                job.print();
                ToastNotification.showSuccess(this, "PDF tersimpan (via " + pdfService.getName() + ")");
                return;
            } catch (PrinterException e) {
                // fall through to dialog
            }
        }

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
                ToastNotification.showSuccess(this, "Detail berhasil dicetak");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal mencetak: " + ex.getMessage());
            }
        }
    }

    private PrintService findPDFPrinter() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService ps : services) {
            String name = ps.getName().toLowerCase();
            if (name.contains("pdf") || name.contains("print to pdf")) {
                return ps;
            }
        }
        return null;
    }
}
