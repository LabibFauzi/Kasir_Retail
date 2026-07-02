package com.kasir.retail.gui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Dialog kamera untuk menangkap (capture) SATU kode barcode dari kemasan produk
 * (barcode retail Indonesia umumnya EAN-13, ada juga EAN-8 / UPC-A / UPC-E / CODE-128).
 * Hasil yang berhasil di-scan dikembalikan lewat callback {@code onCaptured},
 * untuk dipakai sebagai isi field "Kode" pada form Tambah/Edit Produk.
 *
 * Dialog ini berdiri sendiri (tidak menyentuh KasirService / keranjang) sehingga
 * aman dipakai di mana saja yang butuh "baca barcode -> dapat teks-nya".
 */
public class BarcodeCaptureDialog extends JDialog {

    private interface CameraFeed {
        BufferedImage getImage();
        void start();
        void stop();
        boolean isActive();
    }

    private class LocalWebcamFeed implements CameraFeed {
        private final Webcam webcam;
        private boolean started;

        LocalWebcamFeed(Webcam webcam) {
            this.webcam = webcam;
            this.webcam.setViewSize(WebcamResolution.QVGA.getSize());
        }

        @Override
        public BufferedImage getImage() {
            try {
                return webcam.getImage();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public void start() {
            if (!webcam.isOpen()) {
                webcam.open();
            }
            started = true;
        }

        @Override
        public void stop() {
            started = false;
            if (webcam.isOpen()) {
                try {
                    webcam.close();
                } catch (Exception ignored) {}
            }
        }

        @Override
        public boolean isActive() { return started && webcam.isOpen(); }
    }

    private class IpCameraFeed implements CameraFeed {
        private final String streamUrl;
        private volatile BufferedImage currentFrame;
        private Thread readerThread;
        private volatile boolean running;
        private volatile boolean connected;

        IpCameraFeed(String streamUrl) {
            this.streamUrl = streamUrl;
        }

        @Override
        public BufferedImage getImage() { return currentFrame; }

        @Override
        public void start() {
            if (running) return;
            running = true;
            connected = false;
            readerThread = new Thread(() -> {
                while (running) {
                    try {
                        HttpURLConnection conn = (HttpURLConnection) URI.create(streamUrl).toURL().openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(0);
                        conn.connect();
                        connected = true;

                        try (InputStream in = new BufferedInputStream(conn.getInputStream(), 16384)) {
                            ByteArrayOutputStream buf = new ByteArrayOutputStream(65536);
                            int lastByte = -1;
                            boolean foundSOI = false;

                            while (running) {
                                int b = in.read();
                                if (b == -1) break;

                                if (!foundSOI) {
                                    if (lastByte == 0xFF && b == 0xD8) {
                                        buf.write(0xFF);
                                        buf.write(0xD8);
                                        foundSOI = true;
                                    }
                                } else {
                                    buf.write(b);
                                    if (lastByte == 0xFF && b == 0xD9) {
                                        byte[] jpegData = buf.toByteArray();
                                        try {
                                            BufferedImage img = javax.imageio.ImageIO.read(new ByteArrayInputStream(jpegData));
                                            if (img != null) currentFrame = img;
                                        } catch (Exception ignored) {}
                                        buf.reset();
                                        foundSOI = false;
                                    }
                                }
                                lastByte = b;
                            }
                        }
                    } catch (Exception e) {
                        connected = false;
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                }
                connected = false;
            }, "IpCamera-Capture-Reader-Thread");
            readerThread.setDaemon(true);
            readerThread.start();
        }

        @Override
        public void stop() {
            running = false;
            if (readerThread != null) {
                readerThread.interrupt();
                readerThread = null;
            }
            connected = false;
        }

        @Override
        public boolean isActive() { return running && connected; }
    }

    private class CameraPreviewPanel extends JPanel {
        private volatile BufferedImage image;
        private final Timer repaintTimer;

        CameraPreviewPanel() {
            setBackground(Color.BLACK);
            // Timer ini berjalan independen dari thread decoder, jadi preview tetap
            // mulus (~30fps) meskipun proses pembacaan barcode di belakang sedang berat.
            repaintTimer = new Timer(33, e -> {
                if (currentFeed != null) {
                    BufferedImage frame = currentFeed.getImage();
                    if (frame != null) image = frame;
                }
                repaint();
            });
        }

        void startPainting() { repaintTimer.start(); }

        void stopPainting() {
            repaintTimer.stop();
            image = null;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage img = image;
            if (img != null) {
                int w = getWidth(), h = getHeight();
                double scale = Math.min((double) w / img.getWidth(), (double) h / img.getHeight());
                int dw = (int) (img.getWidth() * scale);
                int dh = (int) (img.getHeight() * scale);
                int dx = (w - dw) / 2, dy = (h - dh) / 2;
                g.drawImage(img, dx, dy, dw, dh, null);
            } else {
                g.setColor(Color.DARK_GRAY);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                String msg = "Tidak ada tampilan kamera";
                FontMetrics fm = g.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(msg)) / 2;
                int y = getHeight() / 2;
                g.drawString(msg, x, y);
            }
        }
    }

    private final Consumer<String> onCaptured;

    private CameraFeed currentFeed;
    private CameraPreviewPanel previewPanel;
    private JLabel statusLabel;
    private JLabel resultLabel;
    private JComboBox<String> cameraSelector;
    private JPanel ipConfigPanel;
    private JTextField ipUrlField;
    private JPanel statusPanel;
    private JButton btnUse;
    private JButton btnRescan;

    private final AtomicBoolean scanning = new AtomicBoolean(false);
    private Thread scannerThread;
    private final MultiFormatReader reader = new MultiFormatReader();

    // Anti salah-baca: kode hanya dianggap valid jika terbaca SAMA berturut-turut
    // sebanyak REQUIRED_MATCHES kali. Sekali ada pembacaan yang berbeda, hitungan diulang dari awal.
    private static final int REQUIRED_MATCHES = 2;
    private String pendingCode;
    private int matchCount;

    private List<Webcam> webcams;
    private String capturedCode;
    private static final String OPTION_IP_CAMERA = "IP Camera (Android / URL) ...";

    public BarcodeCaptureDialog(Window parent, Consumer<String> onCaptured) {
        super(parent, "Scan Barcode Kemasan Produk", ModalityType.APPLICATION_MODAL);
        this.onCaptured = onCaptured;

        setSize(520, 620);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        try {
            setIconImage(new ImageIcon(getClass().getResource("/com/kasir/retail/gui/icon.png")).getImage());
        } catch (Exception ignored) {}

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(ThemeManager.BG);

        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        add(mainPanel);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                stopCurrentFeed();
                cleanupAsync();
            }
        });

        loadWebcamsAsync();
    }

    private void loadWebcamsAsync() {
        cameraSelector.addItem("Memuat kamera...");
        cameraSelector.setEnabled(false);

        SwingWorker<List<Webcam>, Void> worker = new SwingWorker<List<Webcam>, Void>() {
            @Override
            protected List<Webcam> doInBackground() {
                return Webcam.getWebcams();
            }

            @Override
            protected void done() {
                try {
                    webcams = get();
                } catch (Exception e) {
                    webcams = null;
                }

                cameraSelector.removeAllItems();
                cameraSelector.setEnabled(true);

                if (webcams != null && !webcams.isEmpty()) {
                    for (Webcam w : webcams) {
                        cameraSelector.addItem(w.getName());
                    }
                    cameraSelector.addItem(OPTION_IP_CAMERA);
                    initDefaultFeed();
                } else {
                    cameraSelector.addItem("Tidak ada webcam lokal");
                    cameraSelector.addItem(OPTION_IP_CAMERA);
                    statusLabel.setText("Tidak ada webcam — gunakan IP Camera");
                    statusLabel.setForeground(ThemeManager.TEXT_SUBTLE);
                }
            }
        };
        worker.execute();
    }

    private void initDefaultFeed() {
        if (webcams != null && !webcams.isEmpty()) {
            Webcam first = webcams.get(0);
            currentFeed = new LocalWebcamFeed(first);
            currentFeed.start();
            cameraSelector.setSelectedIndex(0);
        }
        startScanning();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                ThemeManager.paintRoundedGradient(g, 0, 0, getWidth(), getHeight(), 0,
                        ThemeManager.PRIMARY_GRADIENT_1, ThemeManager.PRIMARY_GRADIENT_2);
            }
        };
        panel.setPreferredSize(new Dimension(520, 64));
        panel.setLayout(new GridBagLayout());

        JLabel title = new JLabel("SCAN BARCODE KEMASAN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        panel.add(title);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(ThemeManager.BG);

        panel.add(createCameraSourceBar(), BorderLayout.NORTH);
        panel.add(createCameraPanel(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCameraSourceBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(ThemeManager.CARD_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblCam = new JLabel("Sumber Kamera:");
        lblCam.setFont(ThemeManager.FONT_BOLD);
        lblCam.setForeground(ThemeManager.TEXT);

        cameraSelector = new JComboBox<>();
        cameraSelector.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        if (cameraSelector.getItemCount() == 0) {
            cameraSelector.addItem("Tidak ada webcam lokal");
        }
        cameraSelector.addItem(OPTION_IP_CAMERA);

        cameraSelector.addActionListener(e -> {
            String sel = (String) cameraSelector.getSelectedItem();
            if (OPTION_IP_CAMERA.equals(sel)) {
                showIpConfig(true);
            } else {
                showIpConfig(false);
                int idx = cameraSelector.getSelectedIndex();
                if (webcams != null && idx >= 0 && idx < webcams.size()) {
                    switchToWebcam(webcams.get(idx));
                }
            }
        });

        bar.add(lblCam, BorderLayout.WEST);
        bar.add(cameraSelector, BorderLayout.CENTER);

        ipConfigPanel = new JPanel(new BorderLayout(6, 0));
        ipConfigPanel.setBackground(ThemeManager.CARD_BG);
        ipConfigPanel.setVisible(false);

        ipUrlField = new JTextField("http://192.168.1.100:8080/video");
        ipUrlField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton btnConnect = ThemeManager.createButton("Hubungkan", ThemeManager.PRIMARY);
        btnConnect.addActionListener(e -> connectToIpCamera());

        ipConfigPanel.add(ipUrlField, BorderLayout.CENTER);
        ipConfigPanel.add(btnConnect, BorderLayout.EAST);

        bar.add(ipConfigPanel, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(ThemeManager.CARD_BG);
        wrapper.add(bar, BorderLayout.NORTH);
        wrapper.add(ipConfigPanel, BorderLayout.SOUTH);

        return wrapper;
    }

    private void showIpConfig(boolean show) {
        ipConfigPanel.setVisible(show);
        revalidate();
        repaint();
    }

    private void switchToWebcam(Webcam webcam) {
        stopCurrentFeed();
        stopScanning();
        resetResult();

        currentFeed = new LocalWebcamFeed(webcam);
        currentFeed.start();

        startScanning();
    }

    private void connectToIpCamera() {
        String url = ipUrlField.getText().trim();
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan URL stream kamera (contoh: http://192.168.1.100:8080/video)", "URL Kosong", JOptionPane.WARNING_MESSAGE);
            return;
        }

        stopCurrentFeed();
        stopScanning();
        resetResult();

        statusLabel.setText("Menghubungkan ke " + url + "...");
        statusLabel.setForeground(ThemeManager.TEXT_SUBTLE);

        currentFeed = new IpCameraFeed(url);
        currentFeed.start();

        Timer checkTimer = new Timer(3000, null);
        checkTimer.addActionListener(e -> {
            if (currentFeed != null && currentFeed.isActive()) {
                statusLabel.setText("Terhubung ke IP Camera");
                statusLabel.setForeground(ThemeManager.SUCCESS);
                ((Timer) e.getSource()).stop();
            } else if (currentFeed instanceof IpCameraFeed) {
                statusLabel.setText("Gagal terhubung, coba lagi...");
                statusLabel.setForeground(ThemeManager.DANGER);
            }
        });
        checkTimer.start();

        startScanning();
    }

    private JPanel createCameraPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER, 1, true));

        previewPanel = new CameraPreviewPanel();

        JPanel overlayPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int scanW = (int) (w * 0.75);
                int scanH = (int) (h * 0.45);
                int x = (w - scanW) / 2;
                int y = (h - scanH) / 2;

                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(0, 0, w, y);
                g2.fillRect(0, y + scanH, w, h - y - scanH);
                g2.fillRect(0, y, x, scanH);
                g2.fillRect(x + scanW, y, w - x - scanW, scanH);

                g2.setColor(ThemeManager.PRIMARY);
                g2.setStroke(new BasicStroke(3));
                int cornerSize = 25;
                g2.drawLine(x, y + cornerSize, x, y);
                g2.drawLine(x, y, x + cornerSize, y);
                g2.drawLine(x + scanW - cornerSize, y, x + scanW, y);
                g2.drawLine(x + scanW, y, x + scanW, y + cornerSize);
                g2.drawLine(x + scanW, y + scanH - cornerSize, x + scanW, y + scanH);
                g2.drawLine(x + scanW - cornerSize, y + scanH, x + scanW, y + scanH);
                g2.drawLine(x, y + scanH - cornerSize, x, y + scanH);
                g2.drawLine(x, y + scanH, x + cornerSize, y + scanH);

                g2.setColor(new Color(255, 0, 0, 150));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(x + 5, y + scanH / 2, x + scanW - 5, y + scanH / 2);

                g2.dispose();
            }
        };
        overlayPanel.setOpaque(false);

        statusLabel = new JLabel("Arahkan barcode kemasan ke dalam kotak", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(0, 0, 0, 160));
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        this.statusPanel = statusPanel;

        overlayPanel.add(statusPanel, BorderLayout.SOUTH);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(460, 300));

        previewPanel.setBounds(0, 0, 460, 300);
        overlayPanel.setBounds(0, 0, 460, 300);

        layeredPane.add(previewPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(overlayPanel, JLayeredPane.PALETTE_LAYER);

        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = layeredPane.getSize();
                previewPanel.setBounds(0, 0, size.width, size.height);
                overlayPanel.setBounds(0, 0, size.width, size.height);
            }
        });

        JPanel cameraContainer = new JPanel(new BorderLayout());
        cameraContainer.setBackground(Color.BLACK);
        cameraContainer.add(layeredPane, BorderLayout.CENTER);

        panel.add(cameraContainer, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(ThemeManager.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.BORDER),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JPanel feedbackPanel = new JPanel(new BorderLayout());
        feedbackPanel.setBackground(ThemeManager.CARD_BG);
        feedbackPanel.setPreferredSize(new Dimension(0, 36));

        resultLabel = new JLabel("Menunggu hasil scan...", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        resultLabel.setForeground(ThemeManager.TEXT_SUBTLE);
        feedbackPanel.add(resultLabel, BorderLayout.CENTER);

        panel.add(feedbackPanel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        btnPanel.setBackground(ThemeManager.CARD_BG);

        btnUse = ThemeManager.createButton("GUNAKAN KODE INI", ThemeManager.SUCCESS);
        btnUse.setPreferredSize(new Dimension(190, 38));
        btnUse.setForeground(Color.WHITE);
        btnUse.addActionListener(e -> useCapturedCode());
        btnPanel.add(btnUse);

        btnRescan = ThemeManager.createButton("Scan Ulang", ThemeManager.INFO);
        btnRescan.setPreferredSize(new Dimension(120, 38));
        btnRescan.setForeground(Color.WHITE);
        btnRescan.addActionListener(e -> resumeScanning());
        btnPanel.add(btnRescan);

        JButton btnCancel = ThemeManager.createButton("Batal", ThemeManager.DANGER);
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.addActionListener(e -> {
            stopCurrentFeed();
            cleanupAsync();
            dispose();
        });
        btnPanel.add(btnCancel);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void useCapturedCode() {
        if (capturedCode == null) return;
        stopCurrentFeed();
        cleanupAsync();
        dispose();
        if (onCaptured != null) {
            onCaptured.accept(capturedCode);
        }
    }

    private void resumeScanning() {
        if (capturedCode == null) return; // belum ada hasil tangkapan, kamera sudah berjalan
        resetResult();
        startScanning();
    }

    private void resetResult() {
        capturedCode = null;
        pendingCode = null;
        matchCount = 0;
        resultLabel.setText("Menunggu hasil scan...");
        resultLabel.setForeground(ThemeManager.TEXT_SUBTLE);
        statusPanel.setVisible(true);
        statusLabel.setText("Arahkan barcode kemasan ke dalam kotak");
        statusLabel.setForeground(Color.WHITE);
    }

    private void stopCurrentFeed() {
        if (currentFeed != null) {
            currentFeed.stop();
            currentFeed = null;
        }
        if (previewPanel != null) {
            previewPanel.stopPainting();
        }
    }

    private void startScanning() {
        if (currentFeed == null) return;

        currentFeed.start();
        previewPanel.startPainting();

        scanning.set(true);

        scannerThread = new Thread(() -> {
            while (scanning.get()) {
                try {
                    if (currentFeed == null || !currentFeed.isActive()) {
                        Thread.sleep(200);
                        continue;
                    }

                    BufferedImage image = currentFeed.getImage();
                    if (image == null) {
                        Thread.sleep(50);
                        continue;
                    }

                    LuminanceSource source = new BufferedImageLuminanceSource(image);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                    Hashtable<DecodeHintType, Object> hints = new Hashtable<>();
                    hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                    Vector<BarcodeFormat> formats = new Vector<>();
                    // Format barcode kemasan retail yang umum dipakai di Indonesia
                    formats.add(BarcodeFormat.EAN_13);
                    formats.add(BarcodeFormat.EAN_8);
                    formats.add(BarcodeFormat.UPC_A);
                    formats.add(BarcodeFormat.UPC_E);
                    formats.add(BarcodeFormat.CODE_39);
                    formats.add(BarcodeFormat.CODE_93);
                    formats.add(BarcodeFormat.CODE_128);
                    formats.add(BarcodeFormat.QR_CODE);
                    hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);

                    Result result = null;
                    try {
                        result = reader.decode(bitmap, hints);
                    } catch (NotFoundException ignored) {}

                    if (result != null) {
                        final String barcode = result.getText();

                        if (barcode.equals(pendingCode)) {
                            matchCount++;
                        } else {
                            pendingCode = barcode;
                            matchCount = 1;
                        }

                        if (matchCount >= REQUIRED_MATCHES) {
                            pendingCode = null;
                            matchCount = 0;
                            onBarcodeDetected(barcode);
                            break; // hentikan loop, tunggu konfirmasi user
                        }
                    } else {
                        // Pembacaan gagal di frame ini -> putuskan rangkaian validasi
                        pendingCode = null;
                        matchCount = 0;
                    }

                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "BarcodeCapture-Decoder-Thread");

        scannerThread.start();
    }

    private void stopScanning() {
        scanning.set(false);
        if (scannerThread != null) {
            scannerThread.interrupt();
            scannerThread = null;
        }
        if (previewPanel != null) {
            previewPanel.stopPainting();
        }
    }

    private void onBarcodeDetected(String barcode) {
        scanning.set(false);
        capturedCode = barcode.trim().toUpperCase();

        java.awt.Toolkit.getDefaultToolkit().beep();

        SwingUtilities.invokeLater(() -> {
            statusPanel.setVisible(false);
            resultLabel.setText("Barcode terdeteksi: " + capturedCode);
            resultLabel.setForeground(ThemeManager.SUCCESS);
        });
    }

    private void cleanupAsync() {
        scanning.set(false);
        if (scannerThread != null) {
            scannerThread.interrupt();
            scannerThread = null;
        }

        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}

            if (currentFeed != null) {
                currentFeed.stop();
                currentFeed = null;
            }
        }, "BarcodeCapture-Cleanup-Thread").start();
    }
}
