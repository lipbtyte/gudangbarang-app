package gudangbarang;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HitungHPP {

    public static double hitungHPP() {
        double totalNilai = 0;
        int totalStok = 0;

        try {
            Connection conn = koneksi.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "SELECT stok, harga_beli FROM barang";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int stok = rs.getInt("stok");
                double hargaBeli = rs.getDouble("harga_beli");
                totalNilai += stok * hargaBeli;
                totalStok += stok;
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        if (totalStok == 0) {
            return 0;
        }

        return totalNilai / totalStok;
    }
}
