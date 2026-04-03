package gudangbarang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Stok extends javax.swing.JFrame {

    private MainMenu parentMenu;
    private ArrayList<String> listKodeBarang;
    
    public Stok(MainMenu parent) {
        this.parentMenu = parent;
        initComponents();
        listKodeBarang = new ArrayList<>();
        setLocationRelativeTo(null);
        loadBarang();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        TextTanggal.setText(sdf.format(new java.util.Date()));
    }

    Stok() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void loadBarang() {
    try {
        Connection conn = koneksi.getConnection();
        Statement stmt = conn.createStatement();
        String sql = "SELECT kode_barang FROM barang";
        ResultSet rs = stmt.executeQuery(sql);

        for (java.awt.event.ActionListener al : cmbPilihBarang.getActionListeners()) {
            cmbPilihBarang.removeActionListener(al);
        }

        cmbPilihBarang.removeAllItems();
        listKodeBarang.clear();

        while (rs.next()) {
            String kode = rs.getString("kode_barang");
            cmbPilihBarang.addItem(kode);
            listKodeBarang.add(kode);
        }

        rs.close();
        stmt.close();
        conn.close();

        if (cmbPilihBarang.getItemCount() > 0) {
            cmbPilihBarang.setSelectedIndex(0);
        }

        // ✅ Tambah listener hanya sekali setelah bersih
        cmbPilihBarang.addActionListener(e -> tampilkanDetailBarang());

    } catch (SQLException e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }
}
    private void tampilkanDetailBarang() {
        if (cmbPilihBarang.getSelectedItem() == null)
            return;

        String kodeBarang = cmbPilihBarang.getSelectedItem().toString();

        try {
            Connection conn = koneksi.getConnection();
            String sql = "SELECT nama_barang, harga_beli FROM barang WHERE kode_barang = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, kodeBarang);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                TextNamaBarang.setText(rs.getString("nama_barang"));
                TextHargaBeli.setText(String.valueOf(rs.getDouble("harga_beli")));
            }

            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tambahBarangBaru() {
        javax.swing.JTextField kodeField = new javax.swing.JTextField();
        javax.swing.JTextField namaField = new javax.swing.JTextField();
        javax.swing.JTextField stokField = new javax.swing.JTextField();
        javax.swing.JTextField hargaField = new javax.swing.JTextField();

        Object[] fields = {
                "Kode Barang:", kodeField,
                "Nama Barang:", namaField,
                "Stok Awal:", stokField,
                "Harga Beli:", hargaField
        };

        int result = javax.swing.JOptionPane.showConfirmDialog(this, fields, "Tambah Barang Baru",
                javax.swing.JOptionPane.OK_CANCEL_OPTION);

        if (result == javax.swing.JOptionPane.OK_OPTION) {
            String kode = kodeField.getText().trim();
            String nama = namaField.getText().trim();
            String stokStr = stokField.getText().trim();
            String hargaStr = hargaField.getText().trim();

            if (kode.isEmpty() || nama.isEmpty() || stokStr.isEmpty() || hargaStr.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Peringatan",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int stok = Integer.parseInt(stokStr);
                double harga = Double.parseDouble(hargaStr);

                Connection conn = koneksi.getConnection();
                String sql = "INSERT INTO barang (kode_barang, nama_barang, stok, harga_beli) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, kode);
                ps.setString(2, nama);
                ps.setInt(3, stok);
                ps.setDouble(4, harga);
                ps.executeUpdate();

                ps.close();
                conn.close();

                javax.swing.JOptionPane.showMessageDialog(this, "Barang berhasil ditambahkan!");
                parentMenu.refreshData();
                this.dispose();
            } catch (NumberFormatException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Stok dan Harga harus berupa angka!", "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void simpanData() {
    if (cmbPilihBarang.getSelectedItem() == null) {
        javax.swing.JOptionPane.showMessageDialog(this, "Pilih barang terlebih dahulu!", "Peringatan",
                javax.swing.JOptionPane.WARNING_MESSAGE);
        return;
    }

    String kodeBarang = cmbPilihBarang.getSelectedItem().toString();
    String stokBaruStr = TextStokBaru.getText().trim();
    String tanggal = TextTanggal.getText().trim();
    String keterangan = TextKeterangan.getText().trim();

    if (stokBaruStr.isEmpty() || tanggal.isEmpty()) {
        javax.swing.JOptionPane.showMessageDialog(this, "Stok baru dan tanggal harus diisi!", "Peringatan",
                javax.swing.JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        int stokBaru = Integer.parseInt(stokBaruStr);
        Connection conn = koneksi.getConnection();
        conn.setAutoCommit(false);

        // Log ke update_stok
        String sqlInsert = "INSERT INTO update_stok (kode_barang, tanggal_update, stok_baru, keterangan) VALUES (?, ?, ?, ?)";
        PreparedStatement psInsert = conn.prepareStatement(sqlInsert);
        psInsert.setString(1, kodeBarang);
        psInsert.setString(2, tanggal);
        psInsert.setInt(3, stokBaru);
        psInsert.setString(4, keterangan);
        psInsert.executeUpdate();
        psInsert.close();

        // ✅ Update stok saja di tabel barang
        String sqlUpdate = "UPDATE barang SET stok = ? WHERE kode_barang = ?";
        PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
        psUpdate.setInt(1, stokBaru);
        psUpdate.setString(2, kodeBarang);
        psUpdate.executeUpdate();
        psUpdate.close();

        conn.commit();
        conn.close();

        javax.swing.JOptionPane.showMessageDialog(this, "Stok berhasil diupdate!");
        parentMenu.refreshData();
        this.dispose();

    } catch (NumberFormatException e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Stok harus berupa angka!", "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
    } catch (SQLException e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        cmbPilihBarang = new javax.swing.JComboBox<>();
        btnTambahBaru = new javax.swing.JButton();
        TextNamaBarang = new javax.swing.JTextField();
        TextHargaBeli = new javax.swing.JTextField();
        TextStokBaru = new javax.swing.JTextField();
        TextTanggal = new javax.swing.JTextField();
        TextKeterangan = new javax.swing.JTextField();
        btnSimpan = new javax.swing.JButton();
        btnBatal = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Update Stok Barang");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("Pilih Barang:");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("Nama Barang:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("Harga Beli:");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("Stok Baru:");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("Tanggal (YYYY-MM-DD):");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("Keterangan:");

        cmbPilihBarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPilihBarangActionPerformed(evt);
            }
        });

        btnTambahBaru.setBackground(new java.awt.Color(51, 51, 255));
        btnTambahBaru.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnTambahBaru.setForeground(new java.awt.Color(255, 255, 255));
        btnTambahBaru.setText("+ Baru");
        btnTambahBaru.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahBaruActionPerformed(evt);
            }
        });

        TextNamaBarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TextNamaBarangActionPerformed(evt);
            }
        });

        TextStokBaru.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TextStokBaruActionPerformed(evt);
            }
        });

        TextTanggal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TextTanggalActionPerformed(evt);
            }
        });

        TextKeterangan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TextKeteranganActionPerformed(evt);
            }
        });

        btnSimpan.setBackground(new java.awt.Color(0, 255, 51));
        btnSimpan.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSimpan.setForeground(new java.awt.Color(255, 255, 255));
        btnSimpan.setText("Simpan");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });

        btnBatal.setBackground(new java.awt.Color(153, 153, 153));
        btnBatal.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnBatal.setForeground(new java.awt.Color(255, 255, 255));
        btnBatal.setText("Batal");
        btnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBatalActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(54, 54, 54)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(cmbPilihBarang, 0, 128, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnTambahBaru, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(TextNamaBarang)
                            .addComponent(TextHargaBeli)
                            .addComponent(TextStokBaru)
                            .addComponent(TextTanggal)
                            .addComponent(TextKeterangan)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(134, 134, 134)
                        .addComponent(btnSimpan)
                        .addGap(34, 34, 34)
                        .addComponent(btnBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(149, 149, 149)
                        .addComponent(jLabel1)))
                .addContainerGap(45, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cmbPilihBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTambahBaru, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(TextNamaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(TextHargaBeli, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(TextStokBaru, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(TextTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(TextKeterangan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnSimpan, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(btnBatal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(23, 23, 23))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbPilihBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPilihBarangActionPerformed
        tampilkanDetailBarang();
    }//GEN-LAST:event_cmbPilihBarangActionPerformed

    private void TextNamaBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TextNamaBarangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TextNamaBarangActionPerformed

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        simpanData();
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBatalActionPerformed
        dispose();
    }//GEN-LAST:event_btnBatalActionPerformed

    private void TextStokBaruActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TextStokBaruActionPerformed
       // TODO add your handling code here:
    }//GEN-LAST:event_TextStokBaruActionPerformed

    private void TextTanggalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TextTanggalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TextTanggalActionPerformed

    private void TextKeteranganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TextKeteranganActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TextKeteranganActionPerformed

    private void btnTambahBaruActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahBaruActionPerformed
        tambahBarangBaru();
    }//GEN-LAST:event_btnTambahBaruActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField TextHargaBeli;
    private javax.swing.JTextField TextKeterangan;
    private javax.swing.JTextField TextNamaBarang;
    private javax.swing.JTextField TextStokBaru;
    private javax.swing.JTextField TextTanggal;
    private javax.swing.JButton btnBatal;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btnTambahBaru;
    private javax.swing.JComboBox<String> cmbPilihBarang;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    // End of variables declaration//GEN-END:variables

    private void refreshData() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
