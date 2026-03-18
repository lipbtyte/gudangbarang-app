GudangBarangApp (Sistem Manajemen Stok) 

Aplikasi manajemen stok berbasis desktop yang dibuat menggunakan Java Swing dan MySQL.
Aplikasi ini digunakan untuk mengelola data barang, melakukan update stok, serta memantau persediaan secara sederhana dan efisien.


Fitur Utama
- Menampilkan daftar barang (tabel stok)
- Menambahkan barang baru
- Update stok barang
- Mencatat riwayat perubahan stok
- Menghapus data barang
- Perhitungan HPP (Harga Pokok Persediaan)


Teknologi yang Digunakan
- Java (Swing GUI)
- MySQL Database
- JDBC (Java Database Connectivity)


Struktur Project
gudangbarang/
│
├── Login.java
├── MainMenu.java
├── Stok.java
├── FormUpdateStok.java
├── HitungHPP.java
├── koneksi.java


Cara Menjalankan

1. Setup Database
Import file `gudangbarang.sql` ke MySQL
Konfigurasi default:
  Host: `localhost`
  User: `root`
  Password: (kosong)

2. Buka Project
Buka project menggunakan NetBeans
Tambahkan MySQL Connector/J (JDBC Driver) ke Libraries

3. Jalankan Aplikasi
Jalankan file:
  `Login.java`
  atau
  `MainMenu.java`


Cara Kerja Aplikasi
- Pengguna mengelola data stok melalui tampilan GUI
- Data disimpan di database MySQL
- Setiap perubahan stok dicatat di tabel `update_stok`
- Data stok utama pada tabel `barang` akan diperbarui secara otomatis


Catatan
Project ini dibuat untuk pembelajaran konsep:
- Pemrograman Berorientasi Objek (PBO)
- Database
- GUI Java

Aplikasi ini masih dapat dikembangkan lebih lanjut menjadi:
- Aplikasi berbasis web (Laravel)
- Sistem berbasis API


Pengembang
Dibuat oleh lipbyte dan EDILAKSO
