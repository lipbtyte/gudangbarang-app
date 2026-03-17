package gudangbarang;

/**
 * Model class untuk menyimpan data transaksi simulasi HPP.
 * Class ini HANYA digunakan untuk simulasi, TIDAK mengubah database.
 * 
 * @author Simulasi HPP Bulanan
 */
public class SimulasiTransaksi {

    // Konstanta untuk jenis transaksi
    public static final String JENIS_PERSEDIAAN_AWAL = "Persediaan Awal";
    public static final String JENIS_PEMBELIAN = "Pembelian";
    public static final String JENIS_PEMAKAIAN = "Pemakaian / Penjualan";

    private String jenis; // Jenis transaksi (Persediaan Awal, Pembelian, Pemakaian)
    private String barang; // Nama/kode barang
    private int qty; // Jumlah unit
    private double harga; // Harga per unit
    private int hari; // Hari ke-n dalam bulan simulasi
    private String keterangan; // Keterangan opsional
    private String tanggal; // Tanggal lengkap untuk display

    // Field tambahan untuk hasil perhitungan
    private double nilaiTotal; // Nilai total = qty * harga (untuk masuk) atau qty * harga HPP (untuk keluar)
    private double hargaHPP; // Harga HPP (untuk pemakaian/penjualan)

    /**
     * Constructor default
     */
    public SimulasiTransaksi() {
    }

    /**
     * Constructor dengan parameter lengkap
     */
    public SimulasiTransaksi(String jenis, String barang, int qty, double harga, int hari, String keterangan) {
        this.jenis = jenis;
        this.barang = barang;
        this.qty = qty;
        this.harga = harga;
        this.hari = hari;
        this.keterangan = keterangan;
        this.nilaiTotal = qty * harga;
        this.hargaHPP = harga;
    }

    // =============== GETTER METHODS ===============

    public String getJenis() {
        return jenis;
    }

    public String getBarang() {
        return barang;
    }

    public int getQty() {
        return qty;
    }

    public double getHarga() {
        return harga;
    }

    public int getHari() {
        return hari;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public String getTanggal() {
        return tanggal;
    }

    public double getNilaiTotal() {
        return nilaiTotal;
    }

    public double getHargaHPP() {
        return hargaHPP;
    }

    // =============== SETTER METHODS ===============

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    public void setBarang(String barang) {
        this.barang = barang;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public void setHari(int hari) {
        this.hari = hari;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public void setNilaiTotal(double nilaiTotal) {
        this.nilaiTotal = nilaiTotal;
    }

    public void setHargaHPP(double hargaHPP) {
        this.hargaHPP = hargaHPP;
    }

    // =============== UTILITY METHODS ===============

    /**
     * Memeriksa apakah transaksi ini adalah transaksi masuk (menambah persediaan)
     */
    public boolean isTransaksiMasuk() {
        return JENIS_PERSEDIAAN_AWAL.equals(jenis) || JENIS_PEMBELIAN.equals(jenis);
    }

    /**
     * Memeriksa apakah transaksi ini adalah transaksi keluar (mengurangi
     * persediaan)
     */
    public boolean isTransaksiKeluar() {
        return JENIS_PEMAKAIAN.equals(jenis);
    }

    @Override
    public String toString() {
        return String.format("SimulasiTransaksi{hari=%d, jenis='%s', barang='%s', qty=%d, harga=%.2f}",
                hari, jenis, barang, qty, harga);
    }
}
