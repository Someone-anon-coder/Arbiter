package session24.problem2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DbSupport {

    private DbSupport() {
    }

    public static Connection openConnection() throws SQLException {
        String url = requireEnv("ARBITER_DB_URL");
        String user = requireEnv("ARBITER_DB_USER");
        String password = requireEnv("ARBITER_DB_PASSWORD");
        return DriverManager.getConnection(url, user, password);
    }

    public static Connection openConnectionWithRewriteBatchedInserts() throws SQLException {
        String baseUrl = requireEnv("ARBITER_DB_URL");
        String url = baseUrl.contains("?")
                ? baseUrl + "&reWriteBatchedInserts=true"
                : baseUrl + "?reWriteBatchedInserts=true";
        String user = requireEnv("ARBITER_DB_USER");
        String password = requireEnv("ARBITER_DB_PASSWORD");
        return DriverManager.getConnection(url, user, password);
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + name);
        }
        return value;
    }

    public static void resetAccountsAndLogs() throws SQLException {
        try (Connection conn = openConnection();
             Statement st = conn.createStatement()) {
            st.execute("TRUNCATE TABLE transfer_log, account RESTART IDENTITY");
            st.execute("""
                    INSERT INTO account (owner, balance) VALUES
                        ('Alice', 1000.00),
                        ('Bob', 100.00),
                        ('Carol', 0.00)
                    """);
        }
    }
}
