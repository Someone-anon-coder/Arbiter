package session24.problem2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class TransferDemo {

    private static final String SQLSTATE_CHECK_VIOLATION = "23514";

    private static final String SQL_DEBIT =
            "UPDATE account SET balance = balance - ? WHERE id = ?";
    private static final String SQL_CREDIT =
            "UPDATE account SET balance = balance + ? WHERE id = ?";
    private static final String SQL_LOG =
            "INSERT INTO transfer_log (from_id, to_id, amount) VALUES (?, ?, ?)";
    private static final String SQL_READ_ACCOUNTS =
            "SELECT id, owner, balance FROM account ORDER BY id";
    private static final String SQL_READ_LOGS =
            "SELECT id, from_id, to_id, amount FROM transfer_log ORDER BY id";

    private TransferDemo() {
    }

    public static void main(String[] args) throws SQLException {
        DbSupport.resetAccountsAndLogs();

        // Step 1: Initial state
        System.out.println("=== Step 1: Initial state ===");
        printStateFromFreshConnection();

        // Step 2: transfer(1, 2, 250.00) -> succeeds
        System.out.println("\n=== Step 2: transfer(1, 2, 250.00) ===");
        runTransferStep(1L, 2L, new BigDecimal("250.00"));
        printStateFromFreshConnection();

        // Step 3: transfer(2, 1, 400.00) -> insufficient funds (23514)
        System.out.println("\n=== Step 3: transfer(2, 1, 400.00) ===");
        runTransferStep(2L, 1L, new BigDecimal("400.00"));
        printStateFromFreshConnection();

        // Step 4: transfer(1, 999, 10.00) -> other failure at credit step
        System.out.println("\n=== Step 4: transfer(1, 999, 10.00) ===");
        runTransferStep(1L, 999L, new BigDecimal("10.00"));
        printStateFromFreshConnection();

        // Step 5: Aborted-transaction demonstration
        System.out.println("\n=== Step 5: Aborted-transaction demonstration (23514 -> 25P02 -> rollback) ===");
        runAbortedTransactionDemo();
        printStateFromFreshConnection();

        // Step 6: transfer(1, 3, 50.00) -> succeeds
        System.out.println("\n=== Step 6: transfer(1, 3, 50.00) ===");
        runTransferStep(1L, 3L, new BigDecimal("50.00"));
        printStateFromFreshConnection();
    }

    public static void transfer(long fromId, long toId, BigDecimal amount)
            throws TransferException, SQLException {
        try (Connection conn = DbSupport.openConnection()) {
            conn.setAutoCommit(false);
            boolean committed = false;
            try {
                // 1. Debit source account
                try (PreparedStatement debitPs = conn.prepareStatement(SQL_DEBIT)) {
                    debitPs.setBigDecimal(1, amount);
                    debitPs.setLong(2, fromId);
                    int updated = debitPs.executeUpdate();
                    if (updated != 1) {
                        throw new TransferException(
                                TransferException.Category.OTHER,
                                "Debit failed: source account id " + fromId + " does not exist");
                    }
                }

                // 2. Credit target account (detect missing target here before log insert)
                try (PreparedStatement creditPs = conn.prepareStatement(SQL_CREDIT)) {
                    creditPs.setBigDecimal(1, amount);
                    creditPs.setLong(2, toId);
                    int updated = creditPs.executeUpdate();
                    if (updated != 1) {
                        throw new TransferException(
                                TransferException.Category.OTHER,
                                "Credit failed: target account id " + toId + " does not exist");
                    }
                }

                // 3. Insert transfer_log row
                try (PreparedStatement logPs = conn.prepareStatement(SQL_LOG)) {
                    logPs.setLong(1, fromId);
                    logPs.setLong(2, toId);
                    logPs.setBigDecimal(3, amount);
                    logPs.executeUpdate();
                }

                conn.commit();
                committed = true;
            } catch (SQLException e) {
                if (SQLSTATE_CHECK_VIOLATION.equals(e.getSQLState())) {
                    throw new InsufficientFundsException(
                            "Insufficient funds for transfer from " + fromId + " to " + toId,
                            e.getSQLState(),
                            e);
                }
                throw new TransferException(
                        TransferException.Category.OTHER,
                        "Database error during transfer (SQLState=" + e.getSQLState() + ")",
                        e.getSQLState(),
                        e);
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
    }

    private static void runTransferStep(long fromId, long toId, BigDecimal amount) throws SQLException {
        try {
            transfer(fromId, toId, amount);
            System.out.printf("  Transfer (%d -> %d, %s) SUCCEEDED%n",
                    fromId, toId, formatMoney(amount));
        } catch (InsufficientFundsException e) {
            System.out.printf("  Transfer (%d -> %d, %s) FAILED [%s]: SQLState=%s, message=%s%n",
                    fromId, toId, formatMoney(amount),
                    e.getCategory(), e.getSqlState(), e.getMessage());
        } catch (TransferException e) {
            System.out.printf("  Transfer (%d -> %d, %s) FAILED [%s]: SQLState=%s, message=%s%n",
                    fromId, toId, formatMoney(amount),
                    e.getCategory(), e.getSqlState(), e.getMessage());
        }
    }

    private static void runAbortedTransactionDemo() throws SQLException {
        try (Connection conn = DbSupport.openConnection()) {
            conn.setAutoCommit(false);
            boolean rolledBack = false;
            try {
                // 1. Failing debit on Bob (id=2, amount=400.00)
                try (PreparedStatement debitPs = conn.prepareStatement(SQL_DEBIT)) {
                    debitPs.setBigDecimal(1, new BigDecimal("400.00"));
                    debitPs.setLong(2, 2L);
                    debitPs.executeUpdate();
                } catch (SQLException e) {
                    System.out.printf("  1) Failing debit caught: SQLState=%s, message=%s%n",
                            e.getSQLState(), e.getMessage());
                }

                // 2. Attempt follow-up SELECT before rolling back -> must fail with 25P02
                try (PreparedStatement selectPs = conn.prepareStatement("SELECT count(*) FROM account");
                     ResultSet rs = selectPs.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("  Unexpectedly succeeded: " + rs.getInt(1));
                    }
                } catch (SQLException e) {
                    System.out.printf("  2) Follow-up SELECT before rollback caught: SQLState=%s, message=%s%n",
                            e.getSQLState(), e.getMessage());
                }

                // 3. Roll back, then run simple SELECT on the same connection
                conn.rollback();
                try (PreparedStatement selectPs = conn.prepareStatement(
                        "SELECT balance FROM account WHERE id = 2");
                     ResultSet rs = selectPs.executeQuery()) {
                    if (rs.next()) {
                        System.out.printf("  3) After rollback, SELECT on same connection succeeded: Bob balance=%s%n",
                                formatMoney(rs.getBigDecimal("balance")));
                    }
                }
                conn.rollback();
                rolledBack = true;
            } finally {
                try {
                    if (!rolledBack) {
                        conn.rollback();
                    }
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        }
    }

    private static void printStateFromFreshConnection() throws SQLException {
        try (Connection freshConn = DbSupport.openConnection()) {
            System.out.print("  Balances: ");
            List<String> balances = new ArrayList<>();
            try (PreparedStatement ps = freshConn.prepareStatement(SQL_READ_ACCOUNTS);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    balances.add(String.format("%s(id=%d)=%s",
                            rs.getString("owner"),
                            rs.getLong("id"),
                            formatMoney(rs.getBigDecimal("balance"))));
                }
            }
            System.out.println(String.join(", ", balances));

            List<String> logs = new ArrayList<>();
            try (PreparedStatement ps = freshConn.prepareStatement(SQL_READ_LOGS);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(String.format("id=%d (%d->%d: %s)",
                            rs.getLong("id"),
                            rs.getLong("from_id"),
                            rs.getLong("to_id"),
                            formatMoney(rs.getBigDecimal("amount"))));
                }
            }
            System.out.printf("  Transfer log rows (%d): %s%n",
                    logs.size(), logs.isEmpty() ? "none" : String.join(", ", logs));
        }
    }

    private static String formatMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }
}
