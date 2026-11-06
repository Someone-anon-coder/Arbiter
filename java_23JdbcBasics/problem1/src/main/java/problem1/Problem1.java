package problem1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Problem1 {
    public static void main(String[] args) throws Exception {
        String url = System.getenv("ARBITER_DB_URL");
        String user = System.getenv("ARBITER_DB_USER");
        String password = System.getenv("ARBITER_DB_PASSWORD");

        // FIX 4: Explicit null checks for environment variables
        if (url == null) throw new IllegalStateException("Environment variable ARBITER_DB_URL is missing.");
        if (user == null) throw new IllegalStateException("Environment variable ARBITER_DB_USER is missing.");
        if (password == null) throw new IllegalStateException("Environment variable ARBITER_DB_PASSWORD is missing.");

        try (Connection c = DriverManager.getConnection(url, user, password)) {
            
            try (PreparedStatement ps = c.prepareStatement("TRUNCATE TABLE lab_item RESTART IDENTITY")) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps = c.prepareStatement("INSERT INTO lab_item (name, quantity) VALUES (?, ?)")) {
                ps.setString(1, "Oscilloscope");
                ps.setInt(2, 3);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = c.prepareStatement("INSERT INTO lab_item (name, quantity, status) VALUES (?, ?, ?)")) {
                ps.setString(1, "Soldering Iron");
                ps.setInt(2, 12);
                ps.setString(3, "USED");
                ps.executeUpdate();

                ps.setString(1, "Multimeter");
                ps.setInt(2, 7);
                ps.setString(3, "BROKEN");
                ps.executeUpdate();
            }

            try (PreparedStatement ps = c.prepareStatement("SELECT id, name, quantity, status FROM lab_item ORDER BY id");
                 ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    int qty = rs.getInt("quantity");
                    String status = rs.getString("status");
                    
                    System.out.printf("%d | %s | %d | %s%n", id, name, qty, status);
                }
            }
        }
    }
}