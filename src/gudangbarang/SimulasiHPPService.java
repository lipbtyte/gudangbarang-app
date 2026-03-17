package gudangbarang;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class untuk menghitung HPP (Harga Pokok Penjualan) secara simulasi.
 * 
 * PENTING: Class ini HANYA melakukan simulasi perhitungan.
 * - TIDAK mengubah database
 * - TIDAK mengubah stok riil
 * - Semua data disimpan dalam memory (ArrayList)
 * 
 * Metode yang didukung:
 * 1. FIFO (First In First Out) - Barang yang pertama masuk, pertama keluar
 * 2. AVERAGE (Weighted Average Cost) - Rata-rata tertimbang
 * 
 * @author Simulasi HPP Bulanan
 */
public class SimulasiHPPService {

    // Konstanta untuk metode HPP
    public static final String METODE_FIFO = "FIFO";
    public static final String METODE_AVERAGE = "AVERAGE";

    // List untuk menyimpan batch persediaan (untuk FIFO)
    private List<BatchPersediaan> batchList;

    // List untuk menyimpan hasil transaksi yang sudah diproses
    private List<SimulasiTransaksi> hasilTransaksi;

    // Variabel untuk tracking nilai
    private double totalPersediaanAwal;
    private double totalPembelian;
    private double totalHPP;
    private double totalPersediaanAkhir;

    // Variabel untuk Average method
    private int totalQtyTersedia;
    private double totalNilaiTersedia;
    private double hargaRataRata;

    /**
     * Constructor - inisialisasi semua list dan reset nilai
     */
    public SimulasiHPPService() {
        batchList = new ArrayList<>();
        hasilTransaksi = new ArrayList<>();
        resetNilai();
    }

    /**
     * Reset semua nilai perhitungan
     */
    public void resetNilai() {
        batchList.clear();
        hasilTransaksi.clear();
        totalPersediaanAwal = 0;
        totalPembelian = 0;
        totalHPP = 0;
        totalPersediaanAkhir = 0;
        totalQtyTersedia = 0;
        totalNilaiTersedia = 0;
        hargaRataRata = 0;
    }

    // =============== PROSES FIFO ===============

    /**
     * Memproses semua transaksi menggunakan metode FIFO.
     * 
     * FIFO Logic:
     * - Setiap pembelian/persediaan awal disimpan sebagai batch terpisah
     * - Saat pemakaian, ambil dari batch paling awal (index 0)
     * - Jika batch habis, lanjut ke batch berikutnya
     * 
     * @param transaksiList List transaksi yang akan diproses
     * @return List hasil transaksi dengan HPP yang sudah dihitung
     */
    public List<SimulasiTransaksi> prosesFIFO(List<SimulasiTransaksi> transaksiList) {
        resetNilai();

        // Urutkan transaksi berdasarkan hari
        transaksiList.sort((a, b) -> Integer.compare(a.getHari(), b.getHari()));

        for (SimulasiTransaksi trx : transaksiList) {
            SimulasiTransaksi hasil = new SimulasiTransaksi();
            hasil.setJenis(trx.getJenis());
            hasil.setBarang(trx.getBarang());
            hasil.setQty(trx.getQty());
            hasil.setHarga(trx.getHarga());
            hasil.setHari(trx.getHari());
            hasil.setTanggal(trx.getTanggal());
            hasil.setKeterangan(trx.getKeterangan());

            if (trx.isTransaksiMasuk()) {
                // Transaksi MASUK: Persediaan Awal atau Pembelian
                prosesMasukFIFO(hasil);
            } else if (trx.isTransaksiKeluar()) {
                // Transaksi KELUAR: Pemakaian/Penjualan
                prosesKeluarFIFO(hasil);
            }

            hasilTransaksi.add(hasil);
        }

        // Hitung persediaan akhir dari sisa batch
        hitungPersediaanAkhirFIFO();

        return hasilTransaksi;
    }

    /**
     * Proses transaksi masuk untuk FIFO - tambahkan batch baru
     */
    private void prosesMasukFIFO(SimulasiTransaksi hasil) {
        // Buat batch baru dan tambahkan ke list
        BatchPersediaan batch = new BatchPersediaan(
                hasil.getQty(),
                hasil.getHarga(),
                hasil.getHari(),
                hasil.getJenis());
        batchList.add(batch);

        // Update total berdasarkan jenis transaksi
        double nilaiMasuk = hasil.getQty() * hasil.getHarga();
        hasil.setNilaiTotal(nilaiMasuk);
        hasil.setHargaHPP(hasil.getHarga()); // Untuk masuk, HPP = harga beli

        if (SimulasiTransaksi.JENIS_PERSEDIAAN_AWAL.equals(hasil.getJenis())) {
            totalPersediaanAwal += nilaiMasuk;
        } else if (SimulasiTransaksi.JENIS_PEMBELIAN.equals(hasil.getJenis())) {
            totalPembelian += nilaiMasuk;
        }
    }

    /**
     * Proses transaksi keluar untuk FIFO - ambil dari batch paling awal
     */
    private void prosesKeluarFIFO(SimulasiTransaksi hasil) {
        int qtyDiminta = hasil.getQty();
        double totalNilaiKeluar = 0;
        int totalQtyKeluar = 0;

        // Ambil dari batch paling awal (FIFO)
        while (qtyDiminta > 0 && !batchList.isEmpty()) {
            BatchPersediaan batchPertama = batchList.get(0);

            if (batchPertama.masihAdaStok()) {
                // Ambil stok dari batch ini
                int qtyDiambil = batchPertama.ambilStok(qtyDiminta);
                double nilaiDiambil = qtyDiambil * batchPertama.getHarga();

                totalNilaiKeluar += nilaiDiambil;
                totalQtyKeluar += qtyDiambil;
                qtyDiminta -= qtyDiambil;

                // Hapus batch jika sudah habis
                if (!batchPertama.masihAdaStok()) {
                    batchList.remove(0);
                }
            } else {
                // Batch kosong, hapus dan lanjut ke batch berikutnya
                batchList.remove(0);
            }
        }

        // Hitung harga HPP rata-rata untuk transaksi ini
        double hargaHPPKeluar = (totalQtyKeluar > 0) ? totalNilaiKeluar / totalQtyKeluar : 0;

        hasil.setHargaHPP(hargaHPPKeluar);
        hasil.setNilaiTotal(totalNilaiKeluar);

        // Tambahkan ke total HPP
        totalHPP += totalNilaiKeluar;
    }

    /**
     * Hitung persediaan akhir dari sisa batch (FIFO)
     */
    private void hitungPersediaanAkhirFIFO() {
        totalPersediaanAkhir = 0;
        for (BatchPersediaan batch : batchList) {
            totalPersediaanAkhir += batch.getNilaiTotal();
        }
    }

    // =============== PROSES AVERAGE ===============

    /**
     * Memproses semua transaksi menggunakan metode Average (Weighted Average Cost).
     * 
     * AVERAGE Logic:
     * - Setiap ada pembelian baru, hitung ulang harga rata-rata tertimbang
     * - Rumus: Harga Rata-rata = Total Nilai Tersedia / Total Qty Tersedia
     * - Saat pemakaian, gunakan harga rata-rata terkini
     * 
     * @param transaksiList List transaksi yang akan diproses
     * @return List hasil transaksi dengan HPP yang sudah dihitung
     */
    public List<SimulasiTransaksi> prosesAverage(List<SimulasiTransaksi> transaksiList) {
        resetNilai();

        // Urutkan transaksi berdasarkan hari
        transaksiList.sort((a, b) -> Integer.compare(a.getHari(), b.getHari()));

        for (SimulasiTransaksi trx : transaksiList) {
            SimulasiTransaksi hasil = new SimulasiTransaksi();
            hasil.setJenis(trx.getJenis());
            hasil.setBarang(trx.getBarang());
            hasil.setQty(trx.getQty());
            hasil.setHarga(trx.getHarga());
            hasil.setHari(trx.getHari());
            hasil.setTanggal(trx.getTanggal());
            hasil.setKeterangan(trx.getKeterangan());

            if (trx.isTransaksiMasuk()) {
                // Transaksi MASUK: Persediaan Awal atau Pembelian
                prosesMasukAverage(hasil);
            } else if (trx.isTransaksiKeluar()) {
                // Transaksi KELUAR: Pemakaian/Penjualan
                prosesKeluarAverage(hasil);
            }

            hasilTransaksi.add(hasil);
        }

        // Hitung persediaan akhir
        hitungPersediaanAkhirAverage();

        return hasilTransaksi;
    }

    /**
     * Proses transaksi masuk untuk Average - update harga rata-rata
     */
    private void prosesMasukAverage(SimulasiTransaksi hasil) {
        int qty = hasil.getQty();
        double harga = hasil.getHarga();
        double nilaiMasuk = qty * harga;

        // Update total qty dan nilai tersedia
        totalQtyTersedia += qty;
        totalNilaiTersedia += nilaiMasuk;

        // Hitung ulang harga rata-rata tertimbang
        // Rumus: Harga Rata-rata = Total Nilai / Total Qty
        hargaRataRata = (totalQtyTersedia > 0) ? totalNilaiTersedia / totalQtyTersedia : 0;

        hasil.setNilaiTotal(nilaiMasuk);
        hasil.setHargaHPP(harga); // Untuk masuk, tampilkan harga beli asli

        if (SimulasiTransaksi.JENIS_PERSEDIAAN_AWAL.equals(hasil.getJenis())) {
            totalPersediaanAwal += nilaiMasuk;
        } else if (SimulasiTransaksi.JENIS_PEMBELIAN.equals(hasil.getJenis())) {
            totalPembelian += nilaiMasuk;
        }
    }

    /**
     * Proses transaksi keluar untuk Average - gunakan harga rata-rata
     */
    private void prosesKeluarAverage(SimulasiTransaksi hasil) {
        int qtyKeluar = hasil.getQty();

        // Gunakan harga rata-rata tertimbang saat ini
        double nilaiKeluar = qtyKeluar * hargaRataRata;

        // Update total qty dan nilai tersedia
        totalQtyTersedia -= qtyKeluar;
        totalNilaiTersedia -= nilaiKeluar;

        // Pastikan tidak negatif (safety check)
        if (totalQtyTersedia < 0)
            totalQtyTersedia = 0;
        if (totalNilaiTersedia < 0)
            totalNilaiTersedia = 0;

        hasil.setHargaHPP(hargaRataRata);
        hasil.setNilaiTotal(nilaiKeluar);

        // Tambahkan ke total HPP
        totalHPP += nilaiKeluar;
    }

    /**
     * Hitung persediaan akhir (Average)
     */
    private void hitungPersediaanAkhirAverage() {
        totalPersediaanAkhir = totalNilaiTersedia;
    }

    // =============== GETTER METHODS UNTUK HASIL ===============

    /**
     * Mendapatkan total HPP yang sudah dihitung
     */
    public double getTotalHPP() {
        return totalHPP;
    }

    /**
     * Mendapatkan total nilai persediaan awal
     */
    public double getTotalPersediaanAwal() {
        return totalPersediaanAwal;
    }

    /**
     * Mendapatkan total nilai pembelian
     */
    public double getTotalPembelian() {
        return totalPembelian;
    }

    /**
     * Mendapatkan total nilai persediaan akhir
     */
    public double getTotalPersediaanAkhir() {
        return totalPersediaanAkhir;
    }

    /**
     * Mendapatkan nilai barang tersedia untuk dijual
     * (Persediaan Awal + Pembelian)
     */
    public double getBarangTersediaUntukDijual() {
        return totalPersediaanAwal + totalPembelian;
    }

    /**
     * Mendapatkan list hasil transaksi yang sudah diproses
     */
    public List<SimulasiTransaksi> getHasilTransaksi() {
        return hasilTransaksi;
    }

    /**
     * Mendapatkan harga rata-rata saat ini (untuk Average method)
     */
    public double getHargaRataRata() {
        return hargaRataRata;
    }

    /**
     * Mendapatkan qty tersisa (untuk Average method)
     */
    public int getQtyTersisa() {
        if (!batchList.isEmpty()) {
            // Untuk FIFO, hitung dari batch list
            int total = 0;
            for (BatchPersediaan batch : batchList) {
                total += batch.getQty();
            }
            return total;
        } else {
            // Untuk Average
            return totalQtyTersedia;
        }
    }

    /**
     * Verifikasi perhitungan HPP
     * HPP = Persediaan Awal + Pembelian - Persediaan Akhir
     */
    public boolean verifikasiHPP() {
        double hppVerifikasi = totalPersediaanAwal + totalPembelian - totalPersediaanAkhir;
        // Toleransi untuk floating point comparison
        return Math.abs(hppVerifikasi - totalHPP) < 0.01;
    }
}
