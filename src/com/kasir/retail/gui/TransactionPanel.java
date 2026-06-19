package com.kasir.retail.gui;

import com.kasir.retail.model.*;
import com.kasir.retail.service.CartItem;
import com.kasir.retail.service.KasirService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;

import static com.kasir.retail.gui.FormatUtil.*;

public class TransactionPanel extends JPanel {
    private final KasirService service;
    private JFrame parentFrame;

    private JTextField searchField;
    private JTable productTable;
    private DefaultTableModel productModel;
    private int hoveredProductRow = -1;

    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel subtotalValue, ppnValue, totalValue;
    private JButton btnBayar, btnQRIS, btnHapus, btnKosong;

    private List<Category> categories;
    private int selectedCategoryId = -1;
    private List<JToggleButton> categoryChips;

    private static final Color STOCK_HIGH  = new Color(0, 201, 167);
    private static final Color STOCK_LOW   = new Color(255, 184, 0);
    private static final Color STOCK_OUT   = new Color(255, 82, 82);
    private static final Color ZEBRA_ODD   = new Color(28, 32, 46);

    public TransactionPanel(KasirService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeManager.BG);

        try {
            categories = service.getAllCategories();
        } catch (SQLException e) {
            categories = new ArrayList<>();
        }

        add(createTopBar(), BorderLayout.NORTH);

        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(ThemeManager.BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        JPanel searchFilterPanel = createSearchFilterPanel();
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 8, 0);
        leftPanel.add(searchFilterPanel, gbc);

        JPanel productPanel = createProductPanel();
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        leftPanel.add(productPanel, gbc);

        JPanel cartSection = createCartSection();

        leftPanel.setPreferredSize(new Dimension(400, 0));

        JPanel centerWrapper = new JPanel(new BorderLayout(8, 0));
        centerWrapper.setBackground(ThemeManager.BG);
        centerWrapper.setBorder(new EmptyBorder(10, 14, 10, 14));
        centerWrapper.add(leftPanel, BorderLayout.CENTER);
        centerWrapper.add(cartSection, BorderLayout.LINE_END);
        add(centerWrapper, BorderLayout.CENTER);

        registerKeyboardShortcuts();
    }

    public void setParentFrame(JFrame frame) {
        this.parentFrame = frame;
    }

    private JPanel createTopBar() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(ThemeManager.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.BORDER),
                new EmptyBorder(10, 20, 10, 20)));

        JLabel title = new JLabel("TRANSAKSI BARU");
        title.setFont(ThemeManager.FONT_TITLE);
        title.setForeground(ThemeManager.TEXT);
        panel.add(title, BorderLayout.WEST);

        JPanel shortcuts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 2));
        shortcuts.setBackground(ThemeManager.CARD_BG);
        shortcuts.add(createShortcutLabel("F1", "Bayar"));
        shortcuts.add(createShortcutLabel("F2", "Cari / Barcode"));
        panel.add(shortcuts, BorderLayout.EAST);

        return panel;
    }

    private JPanel createShortcutLabel(String key, String desc) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 1));
        chip.setBackground(new Color(108, 92, 231, 12));
        chip.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        JLabel keyLabel = new JLabel(key);
        keyLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        keyLabel.setForeground(ThemeManager.PRIMARY);

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(ThemeManager.TEXT_SUBTLE);

        chip.add(keyLabel);
        chip.add(descLabel);
        return chip;
    }

    private JPanel createSearchFilterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeManager.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        JPanel searchRow = new JPanel();
        searchRow.setLayout(new BoxLayout(searchRow, BoxLayout.LINE_AXIS));
        searchRow.setBackground(ThemeManager.CARD_BG);

        JLabel searchIcon = new JLabel("Cari:");
        searchIcon.setFont(ThemeManager.FONT_BOLD);
        searchIcon.setForeground(ThemeManager.TEXT_SUBTLE);
        searchRow.add(searchIcon);

        searchRow.add(Box.createHorizontalStrut(8));

        searchField = new JTextField(14);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setForeground(ThemeManager.TEXT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        searchField.setToolTipText("Ketik nama produk atau scan barcode (Enter = auto-tambah)");
        searchField.addActionListener(e -> handleSmartInput());
        searchRow.add(searchField);

        searchRow.add(Box.createHorizontalGlue());

        JButton btnCamera = ThemeManager.createButton("Kamera", ThemeManager.PRIMARY);
        btnCamera.setPreferredSize(new Dimension(100, 36));
        btnCamera.setMaximumSize(new Dimension(100, 36));
        btnCamera.setToolTipText("Scan barcode menggunakan kamera");
        btnCamera.addActionListener(e -> openCameraScanner());
        searchRow.add(btnCamera);

        searchRow.add(Box.createHorizontalStrut(6));

        JButton btnUpload = ThemeManager.createButton("Upload", ThemeManager.INFO);
        btnUpload.setPreferredSize(new Dimension(100, 36));
        btnUpload.setMaximumSize(new Dimension(100, 36));
        btnUpload.setToolTipText("Upload gambar barcode");
        btnUpload.addActionListener(e -> openImageUploader());
        searchRow.add(btnUpload);

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(searchRow, gbc);

        JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        chipRow.setBackground(ThemeManager.CARD_BG);
        categoryChips = new ArrayList<>();

        JToggleButton allChip = createCategoryChip("Semua", -1);
        allChip.setSelected(true);
        chipRow.add(allChip);
        categoryChips.add(allChip);

        for (Category cat : categories) {
            JToggleButton chip = createCategoryChip(cat.getName(), cat.getId());
            chipRow.add(chip);
            categoryChips.add(chip);
        }

        chipRow.setPreferredSize(new Dimension(0, 30));

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(chipRow, gbc);

        return panel;
    }

    private JToggleButton createCategoryChip(String name, int categoryId) {
        JToggleButton chip = new JToggleButton(name);
        chip.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chip.setForeground(ThemeManager.TEXT_SUBTLE);
        chip.setBackground(new Color(108, 92, 231, 8));
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(108, 92, 231, 30), 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        chip.setFocusPainted(false);
        chip.setCursor(new Cursor(Cursor.HAND_CURSOR));

        chip.addActionListener(e -> {
            for (JToggleButton c : categoryChips) {
                c.setSelected(c == chip);
            }
            selectedCategoryId = chip.isSelected() ? categoryId : -1;
            loadProducts();
        });

        return chip;
    }

    private JPanel createProductPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));

        JLabel sectionTitle = new JLabel("Daftar Produk");
        sectionTitle.setFont(ThemeManager.FONT_SUBTITLE);
        sectionTitle.setForeground(ThemeManager.TEXT);
        sectionTitle.setBorder(new EmptyBorder(0, 4, 6, 0));
        panel.add(sectionTitle, BorderLayout.NORTH);

        String[] cols = {"Kode", "Nama", "Kategori", "Harga", "Stok"};
        productModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        productTable = new JTable(productModel);
        productTable.setRowHeight(36);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        productTable.setShowGrid(false);
        productTable.setIntercellSpacing(new Dimension(0, 0));
        productTable.setBackground(ThemeManager.CARD_BG);
        productTable.setForeground(ThemeManager.TEXT);
        productTable.setSelectionBackground(new Color(108, 92, 231, 30));
        productTable.setSelectionForeground(ThemeManager.TEXT);
        productTable.setRowSelectionAllowed(true);
        productTable.setFocusable(false);
        productTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = productTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(ThemeManager.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(new EmptyBorder(0, 0, 0, 0));
        header.setResizingAllowed(false);
        header.setReorderingAllowed(false);

        TableColumnModel cm = productTable.getColumnModel();
        cm.getColumn(0).setMinWidth(70);
        cm.getColumn(0).setPreferredWidth(90);
        cm.getColumn(1).setMinWidth(120);
        cm.getColumn(1).setPreferredWidth(220);
        cm.getColumn(2).setMinWidth(80);
        cm.getColumn(2).setPreferredWidth(110);
        cm.getColumn(3).setMinWidth(80);
        cm.getColumn(3).setPreferredWidth(110);
        cm.getColumn(4).setMinWidth(50);
        cm.getColumn(4).setPreferredWidth(70);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.CENTER); }
        };
        cm.getColumn(0).setCellRenderer(centerRenderer);
        cm.getColumn(2).setCellRenderer(centerRenderer);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.RIGHT); }
        };
        cm.getColumn(3).setCellRenderer(rightRenderer);

        productTable.setDefaultRenderer(Object.class, new ProductCellRenderer());

        productTable.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = productTable.rowAtPoint(e.getPoint());
                if (row != hoveredProductRow) {
                    hoveredProductRow = row;
                    productTable.repaint();
                }
            }
            public void mouseExited(MouseEvent e) {
                hoveredProductRow = -1;
                productTable.repaint();
            }
        });

        productTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = productTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    String code = productModel.getValueAt(row, 0).toString();
                    quickAddToCart(code);
                }
            }
            public void mouseExited(MouseEvent e) {
                hoveredProductRow = -1;
                productTable.repaint();
            }
        });

        JScrollPane scroll = new JScrollPane(productTable);
        scroll.getViewport().setBackground(ThemeManager.CARD_BG);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scroll, BorderLayout.CENTER);

        loadProducts();
        return panel;
    }

    private JPanel createCartSection() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeManager.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(ThemeManager.CARD_BG);
        JLabel sectionTitle = new JLabel("Keranjang Belanja");
        sectionTitle.setFont(ThemeManager.FONT_SUBTITLE);
        sectionTitle.setForeground(ThemeManager.TEXT);
        titleRow.add(sectionTitle, BorderLayout.WEST);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 4, 6, 0);
        panel.add(titleRow, gbc);

        String[] cols = {"No", "Nama", "Harga", "Qty", "Subtotal", ""};
        cartModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(36);
        cartTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cartTable.setShowGrid(false);
        cartTable.setIntercellSpacing(new Dimension(0, 0));
        cartTable.setBackground(ThemeManager.CARD_BG);
        cartTable.setForeground(ThemeManager.TEXT);
        cartTable.setSelectionBackground(new Color(108, 92, 231, 20));
        cartTable.setSelectionForeground(ThemeManager.TEXT);
        cartTable.setFocusable(false);
        cartTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader cartHeader = cartTable.getTableHeader();
        cartHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartHeader.setBackground(ThemeManager.CARD_BG);
        cartHeader.setForeground(ThemeManager.TEXT_SUBTLE);
        cartHeader.setPreferredSize(new Dimension(0, 30));
        cartHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.BORDER));
        cartHeader.setResizingAllowed(false);
        cartHeader.setReorderingAllowed(false);

        TableColumnModel ccm = cartTable.getColumnModel();
        ccm.getColumn(0).setMaxWidth(36);
        ccm.getColumn(0).setMinWidth(30);
        ccm.getColumn(0).setPreferredWidth(34);
        ccm.getColumn(1).setMinWidth(100);
        ccm.getColumn(1).setPreferredWidth(170);
        ccm.getColumn(2).setMinWidth(70);
        ccm.getColumn(2).setPreferredWidth(95);
        ccm.getColumn(3).setMinWidth(90);
        ccm.getColumn(3).setPreferredWidth(105);
        ccm.getColumn(4).setMinWidth(80);
        ccm.getColumn(4).setPreferredWidth(110);
        ccm.getColumn(5).setMaxWidth(38);
        ccm.getColumn(5).setMinWidth(32);
        ccm.getColumn(5).setPreferredWidth(36);

        DefaultTableCellRenderer centerCart = new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.CENTER); }
        };
        ccm.getColumn(0).setCellRenderer(centerCart);
        ccm.getColumn(3).setCellRenderer(new QtyCellRenderer());

        DefaultTableCellRenderer rightCart = new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.RIGHT); }
        };
        ccm.getColumn(4).setCellRenderer(rightCart);
        ccm.getColumn(5).setCellRenderer(new DeleteCellRenderer());
        cartTable.setDefaultRenderer(Object.class, new CartCellRenderer());

        cartTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int col = cartTable.columnAtPoint(e.getPoint());
                int row = cartTable.rowAtPoint(e.getPoint());
                if (row < 0) return;
                if (col == 3) {
                    handleQtyClick(e, row);
                } else if (col == 5) {
                    handleDeleteClick(row);
                }
            }
        });

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.getViewport().setBackground(ThemeManager.CARD_BG);
        cartScroll.setBorder(BorderFactory.createEmptyBorder());
        cartScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        cartScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        cartScroll.setMinimumSize(new Dimension(0, 80));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 6, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.weightx = 1;
        panel.add(cartScroll, gbc);

        JPanel summaryBar = createSummaryBar();
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(summaryBar, gbc);

        JPanel actionBar = createActionBar();
        gbc.gridy = 3;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(actionBar, gbc);

        return panel;
    }

    private JPanel createSummaryBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 4));
        panel.setBackground(ThemeManager.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, ThemeManager.BORDER),
                new EmptyBorder(6, 10, 6, 10)));

        JLabel subtotalLbl = new JLabel("Subtotal:");
        subtotalLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtotalLbl.setForeground(ThemeManager.TEXT_SUBTLE);
        panel.add(subtotalLbl);

        subtotalValue = new JLabel("Rp0");
        subtotalValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subtotalValue.setForeground(ThemeManager.TEXT);
        panel.add(subtotalValue);

        JLabel ppnLbl = new JLabel("PPN 11%:");
        ppnLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ppnLbl.setForeground(ThemeManager.TEXT_SUBTLE);
        panel.add(ppnLbl);

        ppnValue = new JLabel("Rp0");
        ppnValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ppnValue.setForeground(ThemeManager.ORANGE);
        panel.add(ppnValue);

        JLabel totalLbl = new JLabel("Total:");
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLbl.setForeground(ThemeManager.TEXT);
        panel.add(totalLbl);

        totalValue = new JLabel("Rp0");
        totalValue.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalValue.setForeground(ThemeManager.PRIMARY);
        panel.add(totalValue);

        return panel;
    }

    private JPanel createActionBar() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(ThemeManager.CARD_BG);

        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftGroup.setBackground(ThemeManager.CARD_BG);

        btnHapus = ThemeManager.createButton("Hapus", ThemeManager.DANGER);
        btnHapus.setPreferredSize(new Dimension(110, 36));
        btnHapus.addActionListener(e -> removeFromCart());
        leftGroup.add(btnHapus);

        btnKosong = ThemeManager.createButton("Kosongkan", ThemeManager.ORANGE);
        btnKosong.setPreferredSize(new Dimension(130, 36));
        btnKosong.addActionListener(e -> clearCart());
        leftGroup.add(btnKosong);

        panel.add(leftGroup, BorderLayout.WEST);

        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightGroup.setBackground(ThemeManager.CARD_BG);

        btnQRIS = ThemeManager.createButton("QRIS", ThemeManager.TEAL);
        btnQRIS.setPreferredSize(new Dimension(110, 36));
        btnQRIS.addActionListener(e -> payQRIS());
        rightGroup.add(btnQRIS);

        btnBayar = ThemeManager.createButton("BAYAR (F1)", ThemeManager.PRIMARY);
        btnBayar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBayar.setPreferredSize(new Dimension(160, 40));
        btnBayar.addActionListener(e -> checkout());
        rightGroup.add(btnBayar);

        panel.add(rightGroup, BorderLayout.EAST);

        return panel;
    }

    private void registerKeyboardShortcuts() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "bayar");
        am.put("bayar", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { checkout(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "fokusCari");
        am.put("fokusCari", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                searchField.requestFocus();
                searchField.selectAll();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSearch");
        am.put("clearSearch", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (searchField.getText().length() > 0) {
                    searchField.setText("");
                    loadProducts();
                }
            }
        });
    }

    private void handleSmartInput() {
        String input = searchField.getText().trim().toUpperCase();
        if (input.isEmpty()) {
            loadProducts();
            return;
        }

        boolean looksLikeBarcode = input.length() >= 5 && input.matches("[0-9A-Z]+");
        if (looksLikeBarcode) {
            try {
                Product p = service.getProductByCode(input);
                if (p != null) {
                    service.addToCart(input, 1);
                    refreshCart();
                    ToastNotification.showSuccess(this, p.getName() + " ditambahkan!");
                    searchField.setText("");
                    searchField.requestFocus();
                    return;
                }
            } catch (Exception ex) {
                ToastNotification.showError(this, ex.getMessage());
                return;
            }
        }

        searchProduct();
    }

    private void quickAddToCart(String code) {
        try {
            service.addToCart(code, 1);
            refreshCart();
            ToastNotification.showSuccess(this, "Produk ditambahkan ke keranjang");
        } catch (Exception ex) {
            ToastNotification.showError(this, ex.getMessage());
        }
    }

    private void addProductToCart(String code, int qty) {
        try {
            service.addToCart(code, qty);
            refreshCart();
        } catch (Exception ex) {
            ToastNotification.showError(this, ex.getMessage());
        }
    }

    private void openCameraScanner() {
        if (parentFrame == null) return;
        BarcodeScannerDialog scanner = new BarcodeScannerDialog(parentFrame, service, () -> {
            refreshCart();
            searchField.requestFocus();
        });
        scanner.setVisible(true);
        refreshCart();
    }

    private void openImageUploader() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                BufferedImage img = ImageIO.read(file);
                String code = BarcodeUtil.decode(img);
                if (code != null) {
                    searchField.setText(code.toUpperCase());
                    handleSmartInput();
                } else {
                    JOptionPane.showMessageDialog(this, "Barcode tidak ditemukan!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal membaca gambar: " + ex.getMessage());
            }
        }
    }

    private void loadProducts() {
        try {
            List<Product> list;
            if (selectedCategoryId >= 0) {
                list = service.getProductsByCategory(selectedCategoryId);
            } else {
                String keyword = searchField.getText().trim();
                if (keyword.isEmpty()) {
                    list = service.getAllProducts();
                } else {
                    list = service.searchProducts(keyword);
                }
            }

            productModel.setRowCount(0);
            for (Product p : list) {
                productModel.addRow(new Object[]{
                    p.getCode(), p.getName(), p.getCategoryName(),
                    String.format("Rp%,.0f", p.getPrice()), p
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void searchProduct() {
        loadProducts();
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        int i = 1;
        for (CartItem item : service.getCart()) {
            cartModel.addRow(new Object[]{
                i++,
                item.getProduct().getName(),
                String.format("Rp%,.0f", item.getProduct().getPrice()),
                item,
                String.format("Rp%,.0f", item.getSubtotal()),
                "X"
            });
        }
        updateSummary();
    }

    private void updateSummary() {
        double total = service.getCartTotal();
        double subtotal = total / 1.11;
        double ppn = total - subtotal;

        subtotalValue.setText(String.format("Rp%,.0f", subtotal));
        ppnValue.setText(String.format("Rp%,.0f", ppn));
        totalValue.setText(String.format("Rp%,.0f", total));
    }

    private void handleQtyClick(MouseEvent e, int row) {
        Rectangle cellRect = cartTable.getCellRect(row, 3, false);
        int x = e.getX() - cellRect.x;
        int cellW = cellRect.width;

        CartItem item = service.getCart().get(row);
        try {
            if (x < 32) {
                int newQty = item.getQuantity() - 1;
                service.updateCartQuantity(row, newQty);
                if (newQty <= 0) {
                    ToastNotification.showInfo(this, item.getProduct().getName() + " dihapus");
                }
            } else if (x > cellW - 32) {
                service.updateCartQuantity(row, item.getQuantity() + 1);
            }
            refreshCart();
        } catch (Exception ex) {
            ToastNotification.showError(this, ex.getMessage());
        }
    }

    private void handleDeleteClick(int row) {
        if (row < 0 || row >= service.getCart().size()) return;
        String name = service.getCart().get(row).getProduct().getName();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Hapus " + name + " dari keranjang?", "Konfirmasi",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            service.removeFromCart(row);
            refreshCart();
            ToastNotification.showInfo(this, name + " dihapus");
        }
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih item di keranjang!");
            return;
        }
        handleDeleteClick(row);
    }

    private void clearCart() {
        if (service.isCartEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Kosongkan seluruh keranjang?", "Konfirmasi",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            service.clearCart();
            refreshCart();
            ToastNotification.showInfo(this, "Keranjang dikosongkan");
        }
    }

    private void checkout() {
        if (service.isCartEmpty()) {
            ToastNotification.showError(this, "Keranjang masih kosong!");
            return;
        }
        String input = JOptionPane.showInputDialog(this,
            "Total: Rp" + String.format("%,.0f", service.getCartTotal()) +
            "\nMasukkan jumlah pembayaran:", "Pembayaran Tunai", JOptionPane.PLAIN_MESSAGE);
        if (input == null) return;
        try {
            double payment = parseIndonesianNumber(input);
            Transaction t = service.checkout(payment);
            refreshCart();
            new ReceiptDialog(parentFrame, t, false);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah tidak valid! Gunakan format: 30000 atau 30.000");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void payQRIS() {
        if (service.isCartEmpty()) {
            ToastNotification.showError(this, "Keranjang masih kosong!");
            return;
        }

        double total = service.getCartTotal();
        String invoice = "QRIS-" + java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyMMddHHmmss"));

        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Total pembayaran: Rp%,.0f\nKonfirmasi pembayaran QRIS?", total),
            "Konfirmasi QRIS", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            double payment = total;
            Transaction t = service.checkout(payment);
            refreshCart();

            if (parentFrame != null) {
                new QRISDialog(parentFrame, total, t.getInvoice());
            }
            new ReceiptDialog(parentFrame, t, true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private class ProductCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            boolean isHovered = (row == hoveredProductRow);
            if (isHovered && !isSelected) {
                setBackground(new Color(108, 92, 231, 10));
            } else if (isSelected) {
                setBackground(new Color(108, 92, 231, 30));
            } else {
                setBackground(row % 2 == 0 ? ThemeManager.CARD_BG : ZEBRA_ODD);
            }
            setForeground(ThemeManager.TEXT);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));

            if (col == 4 && value instanceof Product) {
                Product p = (Product) value;
                int stock = p.getStock();
                setHorizontalAlignment(SwingConstants.CENTER);
                if (stock == 0) {
                    setForeground(STOCK_OUT);
                    setText("Habis");
                } else if (stock < 10) {
                    setForeground(STOCK_LOW);
                    setText(String.valueOf(stock));
                } else {
                    setForeground(STOCK_HIGH);
                    setText(String.valueOf(stock));
                }
            } else {
                setForeground(ThemeManager.TEXT);
                setHorizontalAlignment(col == 0 || col == 2 || col == 4 ? SwingConstants.CENTER
                    : col == 3 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                setText(value != null ? value.toString() : "");
            }
            return this;
        }
    }

    private class CartCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            setBackground(isSelected ? new Color(108, 92, 231, 15)
                : (row % 2 == 0 ? ThemeManager.CARD_BG : ZEBRA_ODD));
            setForeground(ThemeManager.TEXT);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));

            if (col == 0) {
                setHorizontalAlignment(SwingConstants.CENTER);
                setText(value != null ? value.toString() : "");
            } else if (col == 1) {
                setHorizontalAlignment(SwingConstants.LEFT);
                setText(value != null ? value.toString() : "");
            } else if (col == 2 || col == 4) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                setText(value != null ? value.toString() : "");
            } else if (col == 3 && value instanceof CartItem) {
                setHorizontalAlignment(SwingConstants.CENTER);
                CartItem item = (CartItem) value;
                setText("- " + item.getQuantity() + " +");
            } else if (col == 5) {
                setHorizontalAlignment(SwingConstants.CENTER);
                setForeground(new Color(180, 120, 120));
                setText("X");
            } else {
                setText(value != null ? value.toString() : "");
            }
            return this;
        }
    }

    private class QtyCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = new JLabel() {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();

                    Color bg = isSelected ? new Color(108, 92, 231, 15)
                        : (row % 2 == 0 ? ThemeManager.CARD_BG : ZEBRA_ODD);
                    g2.setColor(bg);
                    g2.fillRect(0, 0, w, h);

                    CartItem item = service.getCart().get(row);
                    String text = String.valueOf(item.getQuantity());

                    int btnW = 26;
                    int btnH = 24;
                    int y = (h - btnH) / 2;

                    g2.setColor(new Color(108, 92, 231, 25));
                    g2.fillRoundRect(4, y, btnW, btnH, 6, 6);
                    g2.setColor(ThemeManager.PRIMARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("-", 4 + (btnW - fm.stringWidth("-")) / 2, y + btnH - 6);

                    int textX = 4 + btnW + 4;
                    int textW = w - textX - btnW - 4;
                    g2.setColor(ThemeManager.TEXT);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    FontMetrics fm2 = g2.getFontMetrics();
                    g2.drawString(text, textX + (textW - fm2.stringWidth(text)) / 2, y + btnH - 6);

                    int plusX = w - btnW - 4;
                    g2.setColor(new Color(108, 92, 231, 25));
                    g2.fillRoundRect(plusX, y, btnW, btnH, 6, 6);
                    g2.setColor(ThemeManager.PRIMARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    g2.drawString("+", plusX + (btnW - fm.stringWidth("+")) / 2, y + btnH - 6);

                    g2.dispose();
                }
            };
            label.setPreferredSize(new Dimension(105, 34));
            return label;
        }
    }

    private class DeleteCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = new JLabel() {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    Color bg = isSelected ? new Color(108, 92, 231, 15)
                        : (row % 2 == 0 ? ThemeManager.CARD_BG : ZEBRA_ODD);
                    g2.setColor(bg);
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    g2.setColor(new Color(200, 100, 100));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    FontMetrics fm = g2.getFontMetrics();
                    String x = "X";
                    int fx = (getWidth() - fm.stringWidth(x)) / 2;
                    int fy = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(x, fx, fy);

                    g2.dispose();
                }
            };
            label.setPreferredSize(new Dimension(36, 34));
            return label;
        }
    }
}
