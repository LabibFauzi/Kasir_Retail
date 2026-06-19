package com.kasir.retail.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ThemeManager {

    // ─── Brand colours ────────────────────────────────────────────────────────
    public static final Color PRIMARY            = new Color(108, 92, 231);
    public static final Color PRIMARY_DARK       = new Color(84,  70, 202);
    public static final Color PRIMARY_LIGHT      = new Color(136, 124, 255);
    public static final Color PRIMARY_GRADIENT_1 = new Color(108, 92, 231);
    public static final Color PRIMARY_GRADIENT_2 = new Color(162, 84, 220);

    // ─── Sidebar ──────────────────────────────────────────────────────────────
    public static Color SIDEBAR_BG     = new Color(17, 19, 30);
    public static Color SIDEBAR_HOVER  = new Color(30, 33, 48);
    public static Color SIDEBAR_ACTIVE = new Color(108, 92, 231, 35);

    // ─── Status ───────────────────────────────────────────────────────────────
    public static final Color SUCCESS = new Color(0,  201, 167);
    public static final Color DANGER  = new Color(255, 82,  82);
    public static final Color WARNING = new Color(255, 184,  0);
    public static final Color INFO    = new Color(52,  152, 255);
    public static final Color ORANGE  = new Color(255, 150,  50);
    public static final Color TEAL    = new Color(0,   162, 190);

    public static final Color SCAN_SUCCESS = SUCCESS;
    public static final Color SCAN_FAILURE = DANGER;

    // ─── Surface (theme-aware) ────────────────────────────────────────────────
    public static Color BG       = new Color(245, 246, 250);
    public static Color CARD_BG  = Color.WHITE;
    public static Color BORDER   = new Color(228, 230, 240);

    public static Color TEXT        = new Color(30,  33,  48);
    public static Color TEXT_SUBTLE = new Color(100, 108, 130);
    public static Color TEXT_MUTED  = new Color(170, 175, 195);

    // ─── Dark surface constants ────────────────────────────────────────────────
    public static final Color DARK_BG    = new Color(15,  17,  26);
    public static final Color DARK_CARD  = new Color(24,  27,  40);
    public static final Color DARK_CARD2 = new Color(30,  34,  50);
    public static final Color DARK_BORDER= new Color(42,  46,  66);

    // ─── Fonts ────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD     = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO     = new Font("Consolas",  Font.PLAIN, 13);

    // ─── Sizing ───────────────────────────────────────────────────────────────
    public static final int CORNER_RADIUS    = 12;
    public static final int BUTTON_HEIGHT    = 38;
    public static final int TABLE_ROW_HEIGHT = 36;
    public static final int SIDEBAR_WIDTH    = 240;

    public static EmptyBorder PADDING      = new EmptyBorder(16, 16, 16, 16);
    public static EmptyBorder PADDING_CARD = new EmptyBorder(20, 22, 20, 22);

    // ─── Init ─────────────────────────────────────────────────────────────────
    static { applyDarkTheme(); }

    public static boolean isDarkMode() { return true; }

    public static void applyDarkTheme() {
        BG           = DARK_BG;
        CARD_BG      = DARK_CARD;
        BORDER       = DARK_BORDER;
        TEXT         = new Color(232, 235, 250);
        TEXT_SUBTLE  = new Color(140, 148, 180);
        TEXT_MUTED   = new Color(80,  88,  115);
        SIDEBAR_BG   = new Color(12,  14,  22);
        SIDEBAR_HOVER  = new Color(28, 32, 50);
        SIDEBAR_ACTIVE = new Color(108, 92, 231, 45);
    }

    @Deprecated
    public static void applyTheme(boolean isDark) { applyDarkTheme(); }

    // ─── FlatLaf tweaks ───────────────────────────────────────────────────────
    public static void applyFlatLafTweaks() {
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.thumbArc", 10);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(8, 2));
        UIManager.put("ScrollPane.smoothScrolling", true);

        Color inputBg = DARK_CARD;
        UIManager.put("TextField.background",          inputBg);
        UIManager.put("TextField.selectionBackground", new Color(108, 92, 231, 90));
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("PasswordField.background",      inputBg);
        UIManager.put("TextArea.background",           DARK_CARD2);
        UIManager.put("ComboBox.background",           inputBg);
        UIManager.put("ComboBox.buttonBackground",     inputBg);
        UIManager.put("ScrollBar.track",       new Color(20, 23, 35));
        UIManager.put("ScrollBar.thumb",       new Color(52, 58, 84));
        UIManager.put("ScrollBar.thumbHover",  new Color(72, 78, 110));
    }

    // ─── Button helpers ───────────────────────────────────────────────────────
    public static JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed()  ? bg.darker()
                        : getModel().isRollover() ? bg.brighter() : bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(120, BUTTON_HEIGHT));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createIconButton(String text, Color bg, String icon) {
        JButton btn = createButton(icon + "  " + text, bg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(110, BUTTON_HEIGHT - 2));
        return btn;
    }

    /** Hero gradient button — e.g. BAYAR */
    public static JButton createGradientButton(String text, Color c1, Color c2) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);
                Color a = getModel().isPressed() ? c1.darker() : c1;
                Color b = getModel().isPressed() ? c2.darker() : c2;
                g2.setPaint(new GradientPaint(0, 0, a, getWidth(), getHeight(), b));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ─── Table styling ────────────────────────────────────────────────────────
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(TABLE_ROW_HEIGHT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(CARD_BG);
        table.setForeground(TEXT);
        table.setSelectionBackground(new Color(108, 92, 231, 50));
        table.setSelectionForeground(TEXT);

        Color oddRow = new Color(28, 32, 46);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(sel ? new Color(108, 92, 231, 55) : (row % 2 == 0 ? CARD_BG : oddRow));
                setForeground(TEXT);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(28, 32, 50));
        table.getTableHeader().setForeground(TEXT_SUBTLE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
    }

    // ─── Card helper ──────────────────────────────────────────────────────────
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true), PADDING_CARD));
        return card;
    }

    /** Coloured-accent stat card for dashboard summaries */
    public static JPanel createStatCard(String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(16, 22, 16, 22)));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_SUBTLE);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(accent);

        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    // ─── Paint helpers ────────────────────────────────────────────────────────
    public static void paintGradient(Graphics g, int x, int y, int w, int h, Color c1, Color c2) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setPaint(new GradientPaint(x, y, c1, x, y + h, c2));
        g2.fillRect(x, y, w, h);
        g2.dispose();
    }

    public static void paintRoundedGradient(Graphics g, int x, int y, int w, int h, int r, Color c1, Color c2) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);
        g2.setPaint(new GradientPaint(x, y, c1, x + w, y, c2));
        g2.fillRoundRect(x, y, w, h, r, r);
        g2.dispose();
    }
}
