package gudangbarang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

public class MainMenu extends javax.swing.JFrame {

    private DefaultTableModel tableModel;

    public MainMenu() {
        initComponents();
        setLocationRelativeTo(null);
        setupTable();
        loadData();
    }

    private void setupTable() {
        String[] kolom = { "Kode Barang", "Nama Barang", "Stok", "Harga Beli" };
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelBarang.setModel(tableModel);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            Connection conn = koneksi.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM barang";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Object[] row = {
                        rs.getString("kode_barang"),
                        rs.getString("nama_barang"),
                        rs.getInt("stok"),
                        rs.getDouble("harga_beli")
                };
                tableModel.addRow(row);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kembali() {
        new MainMenu().setVisible(true);
        this.dispose();
    }

    public void refreshData() {
        loadData();
    }

    private void hapusBarang() {
        int selectedRow = tabelBarang.getSelectedRow();
        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih barang yang akan dihapus!", "Peringatan",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String kodeBarang = tableModel.getValueAt(selectedRow, 0).toString();

        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Yakin ingin menghapus barang " + kodeBarang + "?", "Konfirmasi",
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            try {
                Connection conn = koneksi.getConnection();

                String sqlUpdate = "DELETE FROM update_stok WHERE kode_barang = ?";
                PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
                psUpdate.setString(1, kodeBarang);
                psUpdate.executeUpdate();
                psUpdate.close();

                String sql = "DELETE FROM barang WHERE kode_barang = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, kodeBarang);
                ps.executeUpdate();

                ps.close();
                conn.close();

                javax.swing.JOptionPane.showMessageDialog(this, "Barang berhasil dihapus!");
                loadData();
            } catch (SQLException e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void tampilkanHPP() {
        // Menampilkan menu pilihan HPP
        String[] options = { "HPP Sederhana", "Simulasi HPP Bulanan", "Batal" };
        int choice = javax.swing.JOptionPane.showOptionDialog(this,
                "Pilih metode perhitungan HPP:",
                "Hitung HPP",
                javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        switch (choice) {
            case 0:
                // HPP Sederhana - menggunakan perhitungan existing
                double hpp = HitungHPP.hitungHPP();
                String hasil = String.format("HPP = Rp %.2f", hpp);
                javax.swing.JOptionPane.showMessageDialog(this, hasil, "Hasil Perhitungan HPP",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                break;
            case 1:
                // Simulasi HPP Bulanan - buka form simulasi
                bukaSimulasiHPPBulanan();
                break;
            default:
                // Batal - tidak melakukan apa-apa
                break;
        }
    }

    /**
     * Membuka form Simulasi HPP Bulanan.
     * Form ini hanya melakukan SIMULASI dan tidak mengubah database.
     */
    private void bukaSimulasiHPPBulanan() {
        SimulasiHPPBulananForm form = new SimulasiHPPBulananForm();
        form.setVisible(true);
    }

    private void prosesLogout() {
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Yakin ingin logout?", "Konfirmasi",
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            new Login().setVisible(true);
            this.dispose();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        jDialog1 = new javax.swing.JDialog();
        jLabel1 = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        btnTambahStok = new javax.swing.JButton();
        btnHapusStok = new javax.swing.JButton();
        btnHitungHPP = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelBarang = new javax.swing.JTable();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
                jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE));
        jFrame1Layout.setVerticalGroup(
                jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE));

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
                jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE));
        jDialog1Layout.setVerticalGroup(
                jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 153));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("GudangBarang");

        btnLogout.setBackground(new java.awt.Color(153, 153, 153));
        btnLogout.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(255, 255, 255));
        btnLogout.setText("Logout");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        btnTambahStok.setBackground(new java.awt.Color(51, 255, 51));
        btnTambahStok.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnTambahStok.setForeground(new java.awt.Color(255, 255, 255));
        btnTambahStok.setText("Tambah Stok");
        btnTambahStok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahStokActionPerformed(evt);
            }
        });

        btnHapusStok.setBackground(new java.awt.Color(255, 0, 0));
        btnHapusStok.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHapusStok.setForeground(new java.awt.Color(255, 255, 255));
        btnHapusStok.setText("Hapus Stok");
        btnHapusStok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusStokActionPerformed(evt);
            }
        });

        btnHitungHPP.setBackground(new java.awt.Color(51, 51, 255));
        btnHitungHPP.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHitungHPP.setForeground(new java.awt.Color(255, 255, 255));
        btnHitungHPP.setText("Hitung HPP");
        btnHitungHPP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHitungHPPActionPerformed(evt);
            }
        });

        tabelBarang.setBackground(new java.awt.Color(255, 255, 204));
        tabelBarang.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null }
                },
                new String[] {
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }));
        jScrollPane1.setViewportView(tabelBarang);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(45, 45, 45)
                                                .addGroup(layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addComponent(btnLogout,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 81,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE)
                                                                .addComponent(btnTambahStok)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(btnHapusStok)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(btnHitungHPP,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 106,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(jScrollPane1,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 452,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(209, 209, 209)
                                                .addComponent(jLabel1)))
                                .addContainerGap(55, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 270,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 37,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnTambahStok, javax.swing.GroupLayout.PREFERRED_SIZE, 37,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnHapusStok, javax.swing.GroupLayout.PREFERRED_SIZE, 37,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnHitungHPP, javax.swing.GroupLayout.PREFERRED_SIZE, 37,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(20, 20, 20)));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnHapusStokActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnHapusStokActionPerformed
        hapusBarang();
    }// GEN-LAST:event_btnHapusStokActionPerformed

    private void btnHitungHPPActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnHitungHPPActionPerformed
        tampilkanHPP();
    }// GEN-LAST:event_btnHitungHPPActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnLogoutActionPerformed
        prosesLogout();
    }// GEN-LAST:event_btnLogoutActionPerformed

    private void btnTambahStokActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnTambahStokActionPerformed
        new Stok(this).setVisible(true);
    }// GEN-LAST:event_btnTambahStokActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Stok.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Stok().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHapusStok;
    private javax.swing.JButton btnHitungHPP;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnTambahStok;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabelBarang;
    // End of variables declaration//GEN-END:variables
}
