package com.kasir.retail.gui;

import com.kasir.retail.model.Transaction;
import com.kasir.retail.model.TransactionItem;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.time.format.DateTimeFormatter;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

public class ReceiptDialog extends JDialog {

    private final Transaction transaction;
    private final boolean isQRIS;

    public ReceiptDialog(JFrame parent, Transaction transaction, boolean isQRIS) {
        super(parent, "Struk Pembayaran", true);
        this.transaction = transaction;
        this.isQRIS = isQRIS;
        setSize(420, 620);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(ThemeManager.CARD_BG);

        JPanel receiptPanel = createReceiptPanel();
        content.add(receiptPanel, BorderLayout.CENTER);

        JPanel actionPanel = createActionPanel();
        content.add(actionPanel, BorderLayout.SOUTH);

        add(content);

        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        setVisible(true);
    }

    private JPanel createReceiptPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(ThemeManager.CARD_BG);
        panel.setBorder(new EmptyBorder(18, 22, 8, 22));

        JPanel header = createHeaderPanel();
        JSeparator sep1 = new JSeparator();
        sep1.setForeground(ThemeManager.BORDER);
        sep1.setBackground(ThemeManager.BORDER);

        JTable itemsTable = createItemsTable();
        JScrollPane itemsScroll = new JScrollPane(itemsTable);
        itemsScroll.setBorder(BorderFactory.createEmptyBorder());
        itemsScroll.getViewport().setBackground(ThemeManager.CARD_BG);
        itemsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        itemsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        itemsScroll.setMinimumSize(new Dimension(0, 60));

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(ThemeManager.BORDER);
        sep2.setBackground(ThemeManager.BORDER);

        JPanel summary = createSummaryPanel();

        panel.add(header, BorderLayout.NORTH);

        JPanel mid = new JPanel(new BorderLayout(0, 4));
        mid.setBackground(ThemeManager.CARD_BG);
        mid.add(sep1, BorderLayout.NORTH);
        mid.add(itemsScroll, BorderLayout.CENTER);
        mid.add(sep2, BorderLayout.SOUTH);
        panel.add(mid, BorderLayout.CENTER);
        panel.add(summary, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHeaderPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(ThemeManager.CARD_BG);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.insets = new Insets(1, 0, 1, 0);

        JLabel store = new JLabel("KASIR RETAIL");
        store.setFont(new Font("Segoe UI", Font.BOLD, 20));
        store.setForeground(ThemeManager.PRIMARY);
        g.gridy = 0;
        p.add(store, g);

        JLabel inv = new JLabel(transaction.getInvoice());
        inv.setFont(new Font("Consolas", Font.PLAIN, 12));
        inv.setForeground(ThemeManager.TEXT_SUBTLE);
        g.gridy = 1;
        p.add(inv, g);

        JLabel date = new JLabel(transaction.getDate()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        date.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        date.setForeground(ThemeManager.TEXT_SUBTLE);
        g.gridy = 2;
        p.add(date, g);

        JLabel method = new JLabel(isQRIS ? "QRIS" : "Tunai");
        method.setFont(new Font("Segoe UI", Font.BOLD, 12));
        method.setForeground(isQRIS ? ThemeManager.TEAL : ThemeManager.SUCCESS);
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        badge.setBackground(ThemeManager.CARD_BG);
        JLabel dot = new JLabel("\u25CF");
        dot.setForeground(isQRIS ? ThemeManager.TEAL : ThemeManager.SUCCESS);
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        badge.add(dot);
        badge.add(method);
        g.gridy = 3;
        g.insets = new Insets(4, 0, 0, 0);
        p.add(badge, g);

        return p;
    }

    private JTable createItemsTable() {
        String[] cols = {"Produk", "Qty", "Harga", "Subtotal"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (TransactionItem item : transaction.getItems()) {
            model.addRow(new Object[]{
                item.getProductName(),
                item.getQuantity(),
                String.format("Rp%,.0f", item.getPrice()),
                String.format("Rp%,.0f", item.getSubtotal())
            });
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(ThemeManager.CARD_BG);
        table.setForeground(ThemeManager.TEXT);
        table.setFocusable(false);
        table.setRowSelectionAllowed(false);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(ThemeManager.CARD_BG);
        table.getTableHeader().setForeground(ThemeManager.TEXT_SUBTLE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 24));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.BORDER));
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.CENTER);

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setMinWidth(120);
        cm.getColumn(0).setPreferredWidth(180);
        cm.getColumn(0).setMaxWidth(250);
        cm.getColumn(1).setMinWidth(30);
        cm.getColumn(1).setPreferredWidth(40);
        cm.getColumn(1).setMaxWidth(50);
        cm.getColumn(2).setMinWidth(60);
        cm.getColumn(2).setPreferredWidth(80);
        cm.getColumn(2).setMaxWidth(100);
        cm.getColumn(3).setMinWidth(60);
        cm.getColumn(3).setPreferredWidth(90);
        cm.getColumn(3).setMaxWidth(110);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.RIGHT); }
        };
        cm.getColumn(2).setCellRenderer(right);
        cm.getColumn(3).setCellRenderer(right);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.CENTER); }
        };
        cm.getColumn(1).setCellRenderer(center);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(ThemeManager.CARD_BG);
                setForeground(ThemeManager.TEXT);
                setBorder(new EmptyBorder(0, 6, 0, 6));
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
                if (col == 1) setHorizontalAlignment(SwingConstants.CENTER);
                else if (col >= 2) setHorizontalAlignment(SwingConstants.RIGHT);
                else setHorizontalAlignment(SwingConstants.LEFT);
                return this;
            }
        });

        return table;
    }

    private JPanel createSummaryPanel() {
        double ppn = transaction.getTotal() * 0.11 / 1.11;
        double subtotal = transaction.getTotal() - ppn;

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(ThemeManager.CARD_BG);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(2, 0, 2, 0);

        g.gridy = 0;
        p.add(summaryRow("Subtotal", String.format("Rp%,.0f", subtotal)), g);

        g.gridy = 1;
        p.add(summaryRow("PPN 11%", String.format("Rp%,.0f", ppn)), g);

        g.gridy = 2;
        JPanel totalRow = new JPanel(new BorderLayout(6, 0));
        totalRow.setBackground(ThemeManager.CARD_BG);
        JLabel tl = new JLabel("TOTAL");
        tl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tl.setForeground(ThemeManager.TEXT);
        totalRow.add(tl, BorderLayout.WEST);
        JLabel tv = new JLabel(String.format("Rp%,.0f", transaction.getTotal()), SwingConstants.RIGHT);
        tv.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tv.setForeground(ThemeManager.PRIMARY);
        totalRow.add(tv, BorderLayout.EAST);
        p.add(totalRow, g);

        g.gridy = 3;
        p.add(summaryRow("Bayar", String.format("Rp%,.0f", transaction.getPayment())), g);

        g.gridy = 4;
        p.add(summaryRow("Kembali", String.format("Rp%,.0f", transaction.getChange())), g);

        return p;
    }

    private JPanel summaryRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(ThemeManager.CARD_BG);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(ThemeManager.TEXT_SUBTLE);
        row.add(l, BorderLayout.WEST);
        JLabel v = new JLabel(value, SwingConstants.RIGHT);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        v.setForeground(ThemeManager.TEXT);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    private JPanel createActionPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 12));
        p.setBackground(ThemeManager.CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.BORDER),
            new EmptyBorder(0, 14, 14, 14)));

        JButton btnCetak = ThemeManager.createButton("Cetak PDF", ThemeManager.PRIMARY);
        btnCetak.setPreferredSize(new Dimension(120, 38));
        btnCetak.addActionListener(e -> printReceipt());

        JButton btnSalin = ThemeManager.createButton("Salin Text", ThemeManager.INFO);
        btnSalin.setPreferredSize(new Dimension(120, 38));
        btnSalin.addActionListener(e -> {
            StringSelection sel = new StringSelection(buildReceiptText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            ToastNotification.showSuccess((JComponent) getContentPane(), "Teks struk disalin");
        });

        JButton btnTutup = ThemeManager.createButton("Tutup", ThemeManager.TEXT_SUBTLE);
        btnTutup.setPreferredSize(new Dimension(100, 38));
        btnTutup.addActionListener(e -> dispose());

        p.add(btnCetak);
        p.add(btnSalin);
        p.add(btnTutup);

        return p;
    }

    private void printReceipt() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Struk-" + transaction.getInvoice());

        String[] lines = buildReceiptText().split("\n");

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            Font font = new Font("Consolas", Font.PLAIN, 9);
            g2.setFont(font);
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
                SwingUtilities.invokeLater(() ->
                    ToastNotification.showSuccess(
                        (JComponent) getContentPane(), "PDF tersimpan (via " + pdfService.getName() + ")"));
                return;
            } catch (PrinterException e) {
                // fall through to dialog
            }
        }

        boolean doPrint = job.printDialog();
        if (doPrint) {
            new Thread(() -> {
                try {
                    job.print();
                    SwingUtilities.invokeLater(() ->
                        ToastNotification.showSuccess(
                            (JComponent) getContentPane(), "Struk berhasil dicetak"));
                } catch (PrinterException e) {
                    SwingUtilities.invokeLater(() ->
                        ToastNotification.showError(
                            (JComponent) getContentPane(), "Gagal mencetak: " + e.getMessage()));
                }
            }).start();
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

    private String buildReceiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("           KASIR RETAIL\n");
        sb.append("========================================\n");
        sb.append(String.format("Invoice : %s\n", transaction.getInvoice()));
        sb.append(String.format("Tanggal : %s\n", transaction.getDateFormatted()));
        sb.append(String.format("Metode  : %s\n", isQRIS ? "QRIS" : "Tunai"));
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-16s %3s %8s\n", "Produk", "Qty", "Subtotal"));
        sb.append("----------------------------------------\n");
        for (TransactionItem item : transaction.getItems()) {
            sb.append(String.format("%-16s %3d Rp%,7.0f\n",
                item.getProductName(), item.getQuantity(), item.getSubtotal()));
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-22s Rp%,8.0f\n", "TOTAL", transaction.getTotal()));
        sb.append(String.format("%-22s Rp%,8.0f\n", "BAYAR", transaction.getPayment()));
        sb.append(String.format("%-22s Rp%,8.0f\n", "KEMBALI", transaction.getChange()));
        sb.append("========================================\n");
        sb.append("         TERIMA KASIH!\n");
        sb.append("========================================\n");
        return sb.toString();
    }
}
