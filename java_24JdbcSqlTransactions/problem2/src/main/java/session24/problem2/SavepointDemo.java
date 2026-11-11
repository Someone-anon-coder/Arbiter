package session24.problem2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

public final class SavepointDemo {

    private static final String SQL_INSERT = "INSERT INTO sp_demo (label) VALUES (?)";
    private static final String SQL_SELECT = "SELECT id, label FROM sp_demo ORDER BY id";

    private SavepointDemo() {
    }

    public static void main(String[] args) throws SQLException {
        resetTable();

        try (Connection conn = DbSupport.openConnection()) {
            conn.setAutoCommit(false);
            boolean committed = false;
            try {
                // 1. Insert alpha
                insertLabel(conn, "alpha");
                System.out.println("1. Inserted 'alpha'");

                // 2. Set first savepoint
                Savepoint sp1 = conn.setSavepoint("sp1");
                System.out.println("2. Set savepoint sp1");

                // 3. Insert gamma (succeeds)
                insertLabel(conn, "gamma");
                System.out.println("3. Inserted 'gamma'");

                // 4. Insert alpha again -> fails on UNIQUE constraint (23505)
                try {
                    insertLabel(conn, "alpha");
                } catch (SQLException e) {
                    System.out.printf("4. Duplicate 'alpha' failed as expected: SQLState=%s, message=%s%n",
                            e.getSQLState(), e.getMessage());
                }

                // 5. Roll back to sp1
                conn.rollback(sp1);
                System.out.println("5. Rolled back to savepoint sp1");

                // 6. Insert beta
                insertLabel(conn, "beta");
                System.out.println("6. Inserted 'beta'");

                // 7. Set second savepoint, insert delta, release second savepoint
                Savepoint sp2 = conn.setSavepoint("sp2");
                insertLabel(conn, "delta");
                conn.releaseSavepoint(sp2);
                System.out.println("7. Set savepoint sp2, inserted 'delta', released sp2");

                // 8. Commit
                conn.commit();
                committed = true;
                System.out.println("8. Committed transaction");
            } finally {
                try {
                    if (!committed) {
                        conn.rollback();
                    }
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        }

        System.out.println("\n=== Final rows in sp_demo (ordered by id) ===");
        try (Connection conn = DbSupport.openConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.printf("  (%d, %s)%n", rs.getInt("id"), rs.getString("label"));
            }
        }
    }

    private static void resetTable() throws SQLException {
        try (Connection conn = DbSupport.openConnection();
             Statement st = conn.createStatement()) {
            st.execute("TRUNCATE TABLE sp_demo RESTART IDENTITY");
        }
    }

    private static void insertLabel(Connection conn, String label) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, label);
            ps.executeUpdate();
        }
    }
}
