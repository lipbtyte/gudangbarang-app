package gudangbarang;

/**
 * Model class untuk menyimpan data batch persediaan.
 * Digunakan dalam perhitungan FIFO untuk melacak setiap batch
 * pembelian/persediaan awal.
 * 
 * FIFO (First In First Out):
 * - Barang yang pertama masuk akan pertama keluar
 * - Setiap batch menyimpan qty dan harga pada saat pembelian/masuk
 * 
 * @author Simulasi HPP Bulanan
 */
public class BatchPersediaan {

    private int qty; // Jumlah unit yang tersisa dalam batch
    private double harga; // Harga per unit pada saat batch ini masuk
    private int hariMasuk; // Hari ke-n saat batch ini masuk
    private String sumber; // Sumber batch (Persediaan Awal atau Pembelian)

    /**
     * Constructor default
     */
    public BatchPersediaan() {
    }

    /**
     * Constructor dengan parameter dasar
     */
    public BatchPersediaan(int qty, double harga) {
        this.qty = qty;
        this.harga = harga;
    }

    /**
     * Constructor dengan parameter lengkap
     */
    public BatchPersediaan(int qty, double harga, int hariMasuk, String sumber) {
        this.qty = qty;
        this.harga = harga;
        this.hariMasuk = hariMasuk;
        this.sumber = sumber;
    }

    // =============== GETTER METHODS ===============

    public int getQty() {
        return qty;
    }

    public double getHarga() {
        return harga;
    }

    public int getHariMasuk() {
        return hariMasuk;
    }

    public String getSumber() {
        return sumber;
    }

    // =============== SETTER METHODS ===============

    public void setQty(int qty) {
        this.qty = qty;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public void setHariMasuk(int hariMasuk) {
        this.hariMasuk = hariMasuk;
    }

    public void setSumber(String sumber) {
        this.sumber = sumber;
    }

    // =============== UTILITY METHODS ===============

    /**
     * Menghitung nilai total batch (qty * harga)
     */
    public double getNilaiTotal() {
        return qty * harga;
    }

    /**
     * Mengurangi qty dari batch ini dan mengembalikan jumlah yang berhasil diambil.
     * Jika qty yang diminta lebih besar dari yang tersedia, hanya qty tersedia yang
     * diambil.
     * 
     * @param jumlahDiminta Jumlah yang ingin diambil
     * @return Jumlah yang berhasil diambil
     */
    public int ambilStok(int jumlahDiminta) {
        int jumlahDiambil = Math.min(jumlahDiminta, qty);
        qty -= jumlahDiambil;
        return jumlahDiambil;
    }

    /**
     * Memeriksa apakah batch ini masih memiliki stok
     */
    public boolean masihAdaStok() {
        return qty > 0;
    }

    /**
     * Membuat salinan dari batch ini
     */
    public BatchPersediaan copy() {
        return new BatchPersediaan(qty, harga, hariMasuk, sumber);
    }

    @Override
    public String toString() {
        return String.format("BatchPersediaan{qty=%d, harga=%.2f, hariMasuk=%d, sumber='%s'}",
                qty, harga, hariMasuk, sumber);
    }
}
