package gudangbarang;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Form untuk Simulasi HPP Bulanan.
 * 
 * PENTING: Form ini HANYA melakukan SIMULASI.
 * - TIDAK mengubah database stok atau transaksi nyata
 * - Semua data hanya tersimpan di memori (ArrayList)
 * - Data akan di-reset saat form ditutup
 * 
 * LAYOUT STRUCTURE:
 * - NORTH: Header Panel (Judul + Pengaturan Simulasi)
 * - CENTER: JSplitPane Vertical
 * - TOP: Input & Daftar Transaksi (JSplitPane Horizontal)
 * - BOTTOM: Output Simulasi (JSplitPane Horizontal)
 * - SOUTH: Footer Panel (Tombol + Total HPP)
 * 
 * @author Simulasi HPP Bulanan
 */
public class SimulasiHPPBulananForm extends javax.swing.JFrame {

    // List untuk menyimpan transaksi simulasi (hanya di memori)
    private List<SimulasiTransaksi> transaksiList;

    // Service untuk perhitungan HPP
    private SimulasiHPPService hppService;

    // Table models
    private DefaultTableModel inputTableModel;
    private DefaultTableModel hasilTableModel;
    private DefaultTableModel ringkasanTableModel;

    // Formatter untuk format mata uang Rupiah
    private DecimalFormat rupiahFormat;

    // List untuk menyimpan data barang dari database (READ ONLY)
    private List<String[]> masterBarang;

    // Interval hari yang dipilih
    private int intervalHari = 5;

    // Bulan dan tahun simulasi
    private int bulanSimulasi;
    private int tahunSimulasi;

    /**
     * Constructor - inisialisasi komponen dan data
     */
    public SimulasiHPPBulananForm() {
        initComponents();
        setLocationRelativeTo(null);

        // Inisialisasi list dan service
        transaksiList = new ArrayList<>();
        hppService = new SimulasiHPPService();
        masterBarang = new ArrayList<>();

        // Setup format Rupiah
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        rupiahFormat = new DecimalFormat("#,##0.00", symbols);

        // Load data barang dari database (READ ONLY)
        loadMasterBarang();

        // Setup combo boxes
        setupComboBoxes();

        // Setup tables
        setupTables();

        // Set bulan dan tahun saat ini
        Calendar cal = Calendar.getInstance();
        bulanSimulasi = cal.get(Calendar.MONTH);
        tahunSimulasi = cal.get(Calendar.YEAR);

        // Event listener untuk reset data saat form ditutup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                resetSemuaData();
            }
        });
    }

    /**
     * Load master barang dari database (READ ONLY - tidak mengubah data)
     */
    private void loadMasterBarang() {
        try {
            Connection conn = koneksi.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT kode_barang, nama_barang, harga_beli FROM barang";
            ResultSet rs = stmt.executeQuery(sql);

            masterBarang.clear();
            while (rs.next()) {
                String[] barang = {
                        rs.getString("kode_barang"),
                        rs.getString("nama_barang"),
                        String.valueOf(rs.getDouble("harga_beli"))
                };
                masterBarang.add(barang);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error load data barang: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Setup semua combo boxes
     */
    private void setupComboBoxes() {
        // Combo Metode HPP
        cmbMetode.removeAllItems();
        cmbMetode.addItem("FIFO");
        cmbMetode.addItem("AVERAGE");

        // Combo Interval Hari
        cmbInterval.removeAllItems();
        cmbInterval.addItem("3");
        cmbInterval.addItem("5");
        cmbInterval.addItem("7");
        cmbInterval.addItem("10");
        cmbInterval.setSelectedIndex(1); // Default: 5

        // Combo Bulan
        cmbBulan.removeAllItems();
        String[] namaBulan = { "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember" };
        for (String bulan : namaBulan) {
            cmbBulan.addItem(bulan);
        }
        cmbBulan.setSelectedIndex(Calendar.getInstance().get(Calendar.MONTH));

        // Combo Tahun
        cmbTahun.removeAllItems();
        int tahunSekarang = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = tahunSekarang - 2; i <= tahunSekarang + 2; i++) {
            cmbTahun.addItem(String.valueOf(i));
        }
        cmbTahun.setSelectedItem(String.valueOf(tahunSekarang));

        // Combo Jenis Transaksi
        cmbJenis.removeAllItems();
        cmbJenis.addItem(SimulasiTransaksi.JENIS_PERSEDIAAN_AWAL);
        cmbJenis.addItem(SimulasiTransaksi.JENIS_PEMBELIAN);
        cmbJenis.addItem(SimulasiTransaksi.JENIS_PEMAKAIAN);

        // Combo Barang
        cmbBarang.removeAllItems();
        for (String[] barang : masterBarang) {
            cmbBarang.addItem(barang[0] + " - " + barang[1]);
        }

        // Setup hari berdasarkan interval
        updateCmbHari();

        // Listener untuk update harga saat barang dipilih
        cmbBarang.addActionListener(e -> updateHargaBarang());

        // Listener untuk update combo hari saat interval berubah
        cmbInterval.addActionListener(e -> updateCmbHari());
    }

    /**
     * Update combo box hari berdasarkan interval yang dipilih
     */
    private void updateCmbHari() {
        cmbHari.removeAllItems();
        intervalHari = Integer.parseInt(cmbInterval.getSelectedItem().toString());

        // Generate hari berdasarkan interval (1, 1+interval, 1+2*interval, ...)
        int hari = 1;
        while (hari <= 31) {
            cmbHari.addItem(String.valueOf(hari));
            hari += intervalHari;
        }
    }

    /**
     * Update harga berdasarkan barang yang dipilih
     */
    private void updateHargaBarang() {
        int selectedIndex = cmbBarang.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < masterBarang.size()) {
            String harga = masterBarang.get(selectedIndex)[2];
            txtHarga.setText(harga);
        }
    }

    /**
     * Setup semua tabel
     */
    private void setupTables() {
        // =========== TABEL INPUT TRANSAKSI ===========
        String[] kolomInput = { "Hari", "Jenis", "Barang", "Qty", "Harga", "Nilai", "Keterangan" };
        inputTableModel = new DefaultTableModel(kolomInput, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelInput.setModel(inputTableModel);
        tabelInput.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setColumnWidths(tabelInput, new int[] { 50, 120, 150, 60, 100, 120, 150 });

        // =========== TABEL HASIL PENCATATAN HPP ===========
        String[] kolomHasil = { "Hari", "Tanggal", "Jenis", "Barang", "Qty Masuk", "Harga Masuk",
                "Qty Keluar", "Harga HPP", "Nilai", "Keterangan" };
        hasilTableModel = new DefaultTableModel(kolomHasil, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelHasil.setModel(hasilTableModel);
        tabelHasil.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setColumnWidths(tabelHasil, new int[] { 50, 100, 120, 150, 80, 100, 80, 100, 120, 150 });

        // =========== TABEL RINGKASAN HPP ===========
        String[] kolomRingkasan = { "Keterangan", "Nilai (Rp)" };
        ringkasanTableModel = new DefaultTableModel(kolomRingkasan, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelRingkasan.setModel(ringkasanTableModel);
        tabelRingkasan.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Styling tabel
        styleTable(tabelInput);
        styleTable(tabelHasil);
        styleTable(tabelRingkasan);
    }

    /**
     * Set column widths for a table
     */
    private void setColumnWidths(JTable table, int[] widths) {
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /**
     * Styling tabel agar lebih menarik
     */
    private void styleTable(JTable table) {
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(new Color(200, 200, 200));
        table.setSelectionBackground(new Color(51, 153, 255));
        table.setSelectionForeground(Color.WHITE);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(51, 102, 153));
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        // Center alignment untuk kolom numerik
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    /**
     * Tambah transaksi ke list simulasi
     */
    private void tambahTransaksi() {
        try {
            // Validasi input
            if (cmbBarang.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Pilih barang terlebih dahulu!",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String qtyText = txtQty.getText().trim();
            String hargaText = txtHarga.getText().trim();

            if (qtyText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Qty harus diisi!",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (hargaText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Harga harus diisi!",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String jenis = cmbJenis.getSelectedItem().toString();
            String barang = cmbBarang.getSelectedItem().toString();
            int qty = Integer.parseInt(qtyText);
            double harga = Double.parseDouble(hargaText);
            int hari = Integer.parseInt(cmbHari.getSelectedItem().toString());
            String keterangan = txtKeterangan.getText().trim();

            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Qty harus lebih dari 0!",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (harga <= 0) {
                JOptionPane.showMessageDialog(this, "Harga harus lebih dari 0!",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Buat transaksi baru
            SimulasiTransaksi trx = new SimulasiTransaksi(jenis, barang, qty, harga, hari, keterangan);

            // Set tanggal berdasarkan bulan dan tahun yang dipilih
            bulanSimulasi = cmbBulan.getSelectedIndex();
            tahunSimulasi = Integer.parseInt(cmbTahun.getSelectedItem().toString());
            String tanggal = String.format("%d-%02d-%02d", tahunSimulasi, bulanSimulasi + 1, hari);
            trx.setTanggal(tanggal);

            // Tambahkan ke list
            transaksiList.add(trx);

            // Update tabel input
            refreshTabelInput();

            // Clear fields
            txtQty.setText("");
            txtKeterangan.setText("");

            JOptionPane.showMessageDialog(this, "Transaksi berhasil ditambahkan ke simulasi!",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Qty dan Harga harus berupa angka yang valid!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Refresh tabel input dengan data transaksi yang sudah dimasukkan
     */
    private void refreshTabelInput() {
        inputTableModel.setRowCount(0);

        for (SimulasiTransaksi trx : transaksiList) {
            Object[] row = {
                    trx.getHari(),
                    trx.getJenis(),
                    trx.getBarang(),
                    trx.getQty(),
                    rupiahFormat.format(trx.getHarga()),
                    rupiahFormat.format(trx.getQty() * trx.getHarga()),
                    trx.getKeterangan()
            };
            inputTableModel.addRow(row);
        }
    }

    /**
     * Hapus transaksi yang dipilih dari list
     */
    private void hapusTransaksi() {
        int selectedRow = tabelInput.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih transaksi yang akan dihapus!",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Yakin ingin menghapus transaksi ini?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            transaksiList.remove(selectedRow);
            refreshTabelInput();
        }
    }

    /**
     * Proses simulasi HPP berdasarkan metode yang dipilih
     */
    private void prosesSimulasi() {
        if (transaksiList.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Tidak ada transaksi untuk diproses!\nSilakan tambahkan transaksi terlebih dahulu.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String metode = cmbMetode.getSelectedItem().toString();
        String judul = txtJudul.getText().trim();

        if (judul.isEmpty()) {
            judul = "Simulasi HPP Bulanan";
        }

        // Update label judul
        lblJudulSimulasi.setText(judul + " - Metode " + metode);

        // Proses berdasarkan metode yang dipilih
        List<SimulasiTransaksi> hasil;
        if (SimulasiHPPService.METODE_FIFO.equals(metode)) {
            hasil = hppService.prosesFIFO(new ArrayList<>(transaksiList));
        } else {
            hasil = hppService.prosesAverage(new ArrayList<>(transaksiList));
        }

        // Tampilkan hasil di tabel pencatatan
        tampilkanHasilPencatatan(hasil);

        // Tampilkan ringkasan HPP
        tampilkanRingkasan(metode);

        // Tampilkan total HPP
        double totalHPP = hppService.getTotalHPP();
        lblTotalHPP.setText("TOTAL HPP (" + metode + ") : Rp " + rupiahFormat.format(totalHPP));

        JOptionPane.showMessageDialog(this,
                "Simulasi HPP berhasil diproses!\nTotal HPP: Rp " + rupiahFormat.format(totalHPP),
                "Hasil Simulasi", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Tampilkan hasil pencatatan HPP di tabel
     */
    private void tampilkanHasilPencatatan(List<SimulasiTransaksi> hasil) {
        hasilTableModel.setRowCount(0);

        for (SimulasiTransaksi trx : hasil) {
            Object[] row = new Object[10];
            row[0] = trx.getHari();
            row[1] = trx.getTanggal();
            row[2] = trx.getJenis();
            row[3] = trx.getBarang();

            if (trx.isTransaksiMasuk()) {
                // Transaksi masuk: tampilkan di kolom Qty Masuk dan Harga Masuk
                row[4] = trx.getQty();
                row[5] = rupiahFormat.format(trx.getHarga());
                row[6] = "-";
                row[7] = "-";
            } else {
                // Transaksi keluar: tampilkan di kolom Qty Keluar dan Harga HPP
                row[4] = "-";
                row[5] = "-";
                row[6] = trx.getQty();
                row[7] = rupiahFormat.format(trx.getHargaHPP());
            }

            row[8] = rupiahFormat.format(trx.getNilaiTotal());
            row[9] = trx.getKeterangan();

            hasilTableModel.addRow(row);
        }
    }

    /**
     * Tampilkan ringkasan HPP di tabel
     */
    private void tampilkanRingkasan(String metode) {
        ringkasanTableModel.setRowCount(0);

        Object[][] ringkasan = {
                { "Persediaan Awal", rupiahFormat.format(hppService.getTotalPersediaanAwal()) },
                { "+ Pembelian", rupiahFormat.format(hppService.getTotalPembelian()) },
                { "= Barang Tersedia Untuk Dijual", rupiahFormat.format(hppService.getBarangTersediaUntukDijual()) },
                { "- Persediaan Akhir", rupiahFormat.format(hppService.getTotalPersediaanAkhir()) },
                { "--------------------------------", "--------------------------------" },
                { "HPP (" + metode + ")", rupiahFormat.format(hppService.getTotalHPP()) }
        };

        for (Object[] row : ringkasan) {
            ringkasanTableModel.addRow(row);
        }
    }

    /**
     * Reset semua data simulasi
     */
    private void resetSemuaData() {
        transaksiList.clear();
        hppService.resetNilai();
        inputTableModel.setRowCount(0);
        hasilTableModel.setRowCount(0);
        ringkasanTableModel.setRowCount(0);
        lblTotalHPP.setText("TOTAL HPP : Rp 0");
        lblJudulSimulasi.setText("Hasil Simulasi HPP Bulanan");
        txtJudul.setText("");
        txtQty.setText("");
        txtHarga.setText("");
        txtKeterangan.setText("");
    }

    /**
     * Inisialisasi komponen UI dengan layout yang proper
     */
    private void initComponents() {
        // ========== FRAME SETUP ==========
        setTitle("Simulasi HPP Bulanan");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 750));
        setPreferredSize(new Dimension(1300, 850));
        getContentPane().setBackground(new Color(240, 240, 245));

        // Main layout: BorderLayout
        setLayout(new BorderLayout(10, 10));

        // ========== NORTH: HEADER PANEL ==========
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // ========== CENTER: MAIN CONTENT (JSplitPane Vertical) ==========
        JSplitPane mainSplitPane = createMainContent();
        add(mainSplitPane, BorderLayout.CENTER);

        // ========== SOUTH: FOOTER PANEL ==========
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        pack();
    }

    /**
     * Create header panel dengan pengaturan simulasi
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(new Color(240, 240, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

        // Title
        lblTitle = new JLabel("SIMULASI HPP BULANAN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(51, 102, 153));
        panel.add(lblTitle, BorderLayout.NORTH);

        // Settings panel dengan GridBagLayout
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBackground(new Color(240, 240, 245));
        settingsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                "Pengaturan Simulasi",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(51, 102, 153)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Judul Simulasi
        gbc.gridx = 0;
        gbc.gridy = 0;
        settingsPanel.add(new JLabel("Judul Simulasi:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        txtJudul = new JTextField(25);
        settingsPanel.add(txtJudul, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 4;
        settingsPanel.add(new JLabel("Metode HPP:"), gbc);
        gbc.gridx = 5;
        cmbMetode = new JComboBox<>();
        cmbMetode.setPreferredSize(new Dimension(100, 25));
        settingsPanel.add(cmbMetode, gbc);

        // Row 1: Interval, Bulan, Tahun
        gbc.gridx = 0;
        gbc.gridy = 1;
        settingsPanel.add(new JLabel("Interval Hari:"), gbc);
        gbc.gridx = 1;
        cmbInterval = new JComboBox<>();
        cmbInterval.setPreferredSize(new Dimension(60, 25));
        settingsPanel.add(cmbInterval, gbc);

        gbc.gridx = 2;
        settingsPanel.add(new JLabel("Bulan:"), gbc);
        gbc.gridx = 3;
        cmbBulan = new JComboBox<>();
        cmbBulan.setPreferredSize(new Dimension(110, 25));
        settingsPanel.add(cmbBulan, gbc);

        gbc.gridx = 4;
        settingsPanel.add(new JLabel("Tahun:"), gbc);
        gbc.gridx = 5;
        cmbTahun = new JComboBox<>();
        cmbTahun.setPreferredSize(new Dimension(80, 25));
        settingsPanel.add(cmbTahun, gbc);

        panel.add(settingsPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create main content dengan JSplitPane
     */
    private JSplitPane createMainContent() {
        // ========== TOP SECTION: Input & Daftar Transaksi ==========
        JSplitPane topSplitPane = createTopSection();

        // ========== BOTTOM SECTION: Output Simulasi ==========
        JSplitPane bottomSplitPane = createBottomSection();

        // Main vertical split
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplitPane, bottomSplitPane);
        mainSplitPane.setResizeWeight(0.45);
        mainSplitPane.setDividerLocation(300);
        mainSplitPane.setDividerSize(8);
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        return mainSplitPane;
    }

    /**
     * Create top section: Input Panel (left) + Daftar Transaksi (right)
     */
    private JSplitPane createTopSection() {
        // LEFT: Input Form
        JPanel inputPanel = createInputPanel();

        // RIGHT: Daftar Transaksi
        JPanel daftarPanel = createDaftarTransaksiPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, daftarPanel);
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerLocation(400);
        splitPane.setDividerSize(6);
        splitPane.setContinuousLayout(true);

        return splitPane;
    }

    /**
     * Create input form panel
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                "Input Transaksi Simulasi",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(51, 102, 153)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Jenis Transaksi
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Jenis Transaksi:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        cmbJenis = new JComboBox<>();
        panel.add(cmbJenis, gbc);
        gbc.gridwidth = 1;
        row++;

        // Barang
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Barang:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        cmbBarang = new JComboBox<>();
        panel.add(cmbBarang, gbc);
        gbc.gridwidth = 1;
        row++;

        // Qty
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Qty:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtQty = new JTextField(10);
        panel.add(txtQty, gbc);
        gbc.gridwidth = 1;
        row++;

        // Harga
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Harga:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtHarga = new JTextField(10);
        panel.add(txtHarga, gbc);
        gbc.gridwidth = 1;
        row++;

        // Hari ke-
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Hari ke-:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        cmbHari = new JComboBox<>();
        panel.add(cmbHari, gbc);
        gbc.gridwidth = 1;
        row++;

        // Keterangan
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Keterangan:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtKeterangan = new JTextField(15);
        panel.add(txtKeterangan, gbc);
        gbc.gridwidth = 1;
        row++;

        // Buttons panel
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(Color.WHITE);

        btnTambah = new JButton("Tambah ke Tabel");
        btnTambah.setBackground(new Color(0, 200, 83));
        btnTambah.setForeground(Color.WHITE);
        btnTambah.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTambah.setPreferredSize(new Dimension(140, 35));
        btnTambah.addActionListener(e -> tambahTransaksi());
        buttonPanel.add(btnTambah);

        btnHapus = new JButton("Hapus Transaksi");
        btnHapus.setBackground(new Color(255, 82, 82));
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHapus.setPreferredSize(new Dimension(140, 35));
        btnHapus.addActionListener(e -> hapusTransaksi());
        buttonPanel.add(btnHapus);

        panel.add(buttonPanel, gbc);

        // Add filler to push content up
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    /**
     * Create daftar transaksi panel dengan table
     */
    private JPanel createDaftarTransaksiPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                "Daftar Transaksi Simulasi",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(51, 102, 153)));

        tabelInput = new JTable();
        scrollInput = new JScrollPane(tabelInput);
        scrollInput.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollInput.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.add(scrollInput, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create bottom section: Kartu Persediaan (left) + Ringkasan (right)
     */
    private JSplitPane createBottomSection() {
        // LEFT: Kartu Persediaan / Pencatatan HPP
        JPanel kartuPanel = createKartuPersediaanPanel();

        // RIGHT: Ringkasan HPP
        JPanel ringkasanPanel = createRingkasanPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, kartuPanel, ringkasanPanel);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerLocation(750);
        splitPane.setDividerSize(6);
        splitPane.setContinuousLayout(true);

        return splitPane;
    }

    /**
     * Create kartu persediaan panel
     */
    private JPanel createKartuPersediaanPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                "Tabel Pencatatan HPP (Kartu Persediaan)",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(51, 102, 153)));

        // Label judul simulasi
        lblJudulSimulasi = new JLabel("Hasil Simulasi HPP Bulanan");
        lblJudulSimulasi.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblJudulSimulasi.setForeground(new Color(51, 102, 153));
        lblJudulSimulasi.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(lblJudulSimulasi, BorderLayout.NORTH);

        tabelHasil = new JTable();
        scrollHasil = new JScrollPane(tabelHasil);
        scrollHasil.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollHasil.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.add(scrollHasil, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create ringkasan HPP panel
     */
    private JPanel createRingkasanPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                "Ringkasan HPP Bulanan",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(51, 102, 153)));

        tabelRingkasan = new JTable();
        scrollRingkasan = new JScrollPane(tabelRingkasan);
        scrollRingkasan.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollRingkasan.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.add(scrollRingkasan, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create footer panel dengan tombol aksi dan total HPP
     */
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 5));
        panel.setBackground(new Color(240, 240, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        // Left: Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        buttonPanel.setBackground(new Color(240, 240, 245));

        btnProses = new JButton("PROSES SIMULASI HPP");
        btnProses.setBackground(new Color(156, 39, 176));
        btnProses.setForeground(Color.WHITE);
        btnProses.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnProses.setPreferredSize(new Dimension(200, 45));
        btnProses.addActionListener(e -> prosesSimulasi());
        buttonPanel.add(btnProses);

        btnReset = new JButton("Reset Semua");
        btnReset.setBackground(new Color(255, 152, 0));
        btnReset.setForeground(Color.WHITE);
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReset.setPreferredSize(new Dimension(120, 40));
        btnReset.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Yakin ingin mereset semua data simulasi?", "Konfirmasi",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                resetSemuaData();
            }
        });
        buttonPanel.add(btnReset);

        btnKembali = new JButton("Kembali");
        btnKembali.setBackground(new Color(158, 158, 158));
        btnKembali.setForeground(Color.WHITE);
        btnKembali.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnKembali.setPreferredSize(new Dimension(100, 40));
        btnKembali.addActionListener(e -> {
            resetSemuaData();
            dispose();
        });
        buttonPanel.add(btnKembali);

        panel.add(buttonPanel, BorderLayout.WEST);

        // Right: Total HPP
        lblTotalHPP = new JLabel("TOTAL HPP : Rp 0", SwingConstants.RIGHT);
        lblTotalHPP.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotalHPP.setForeground(new Color(0, 128, 0));
        lblTotalHPP.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        panel.add(lblTotalHPP, BorderLayout.EAST);

        return panel;
    }

    // ========== Variables Declaration ==========
    private JLabel lblTitle;
    private JLabel lblJudulSimulasi;
    private JLabel lblTotalHPP;

    private JTextField txtJudul;
    private JTextField txtQty;
    private JTextField txtHarga;
    private JTextField txtKeterangan;

    private JComboBox<String> cmbMetode;
    private JComboBox<String> cmbInterval;
    private JComboBox<String> cmbBulan;
    private JComboBox<String> cmbTahun;
    private JComboBox<String> cmbJenis;
    private JComboBox<String> cmbBarang;
    private JComboBox<String> cmbHari;

    private JButton btnTambah;
    private JButton btnHapus;
    private JButton btnProses;
    private JButton btnReset;
    private JButton btnKembali;

    private JTable tabelInput;
    private JTable tabelHasil;
    private JTable tabelRingkasan;

    private JScrollPane scrollInput;
    private JScrollPane scrollHasil;
    private JScrollPane scrollRingkasan;

    /**
     * Main method for testing
     */
    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SimulasiHPPBulananForm.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            new SimulasiHPPBulananForm().setVisible(true);
        });
    }
}
