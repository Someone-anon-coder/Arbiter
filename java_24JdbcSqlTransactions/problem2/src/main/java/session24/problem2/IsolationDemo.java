package session24.problem2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class IsolationDemo {

    private static final String SQL_ALICE_BALANCE =
            "SELECT balance FROM account WHERE id = 1";
    private static final String SQL_ACCOUNT_COUNT =
            "SELECT COUNT(*) FROM account";
    private static final String SQL_B_UPDATE_ALICE =
            "UPDATE account SET balance = balance + ? WHERE id = 1";
    private static final String SQL_B_INSERT_DAVE =
            "INSERT INTO account (owner, balance) VALUES (?, ?)";

    private IsolationDemo() {
    }

    public static void main(String[] args) throws SQLException {
        runIsolationSequence(Connection.TRANSACTION_READ_COMMITTED, "READ COMMITTED");
        System.out.println();
        runIsolationSequence(Connection.TRANSACTION_REPEATABLE_READ, "REPEATABLE READ");
        System.out.println();
        runReadUncommittedExperiment();
    }

    private static void runIsolationSequence(int isolationLevel, String levelName) throws SQLException {
        DbSupport.resetAccountsAndLogs();
        System.out.println("=== Isolation Level: " + levelName + " ===");

        try (Connection connA = DbSupport.openConnection();
             Connection connB = DbSupport.openConnection()) {

            connA.setAutoCommit(false);
            connA.setTransactionIsolation(isolationLevel);
            connB.setAutoCommit(true);

            boolean committed = false;
            try {
                // Step 1: A reads Alice's balance and account row count
                Snapshot step1 = readSnapshot(connA);
                System.out.printf("  Step 1 (A initial read):             Alice=%s, %d rows%n",
                        step1.aliceBalance(), step1.rowCount());

                // Step 2: B adds 100.00 to Alice and inserts Dave (10.00) in auto-commit mode
                try (PreparedStatement updatePs = connB.prepareStatement(SQL_B_UPDATE_ALICE);
                     PreparedStatement insertPs = connB.prepareStatement(SQL_B_INSERT_DAVE)) {
                    updatePs.setBigDecimal(1, new BigDecimal("100.00"));
                    updatePs.executeUpdate();

                    insertPs.setString(1, "Dave");
                    insertPs.setBigDecimal(2, new BigDecimal("10.00"));
                    insertPs.executeUpdate();
                }
                System.out.println("  Step 2 (B committed):                Alice +100.00, inserted Dave=10.00");

                // Step 3: A reads Alice's balance and row count again inside the same transaction
                Snapshot step3 = readSnapshot(connA);
                System.out.printf("  Step 3 (A second read in same tx):   Alice=%s, %d rows%n",
                        step3.aliceBalance(), step3.rowCount());

                // Step 4: A commits, then reads both values once more
                connA.commit();
                Snapshot step4 = readSnapshot(connA);
                System.out.printf("  Step 4 (A read after commit):        Alice=%s, %d rows%n",
                        step4.aliceBalance(), step4.rowCount());
                connA.commit();
                committed = true;
            } finally {
                try {
                    if (!committed) {
                        connA.rollback();
                    }
                } finally {
                    connA.setAutoCommit(true);
                }
            }
        }
    }

    private static void runReadUncommittedExperiment() throws SQLException {
        DbSupport.resetAccountsAndLogs();
        System.out.println("=== READ UNCOMMITTED One-Line Experiment ===");
        try (Connection connA = DbSupport.openConnection();
             Connection connB = DbSupport.openConnection()) {

            connA.setAutoCommit(false);
            connA.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            connB.setAutoCommit(false);

            boolean aFinished = false;
            boolean bFinished = false;
            try {
                // B makes an uncommitted change to Alice (+500.00 -> 1500.00)
                try (PreparedStatement updatePs = connB.prepareStatement(SQL_B_UPDATE_ALICE)) {
                    updatePs.setBigDecimal(1, new BigDecimal("500.00"));
                    updatePs.executeUpdate();
                }

                String reportedLevel;
                try (PreparedStatement showPs = connA.prepareStatement("SHOW transaction_isolation");
                     ResultSet rs = showPs.executeQuery()) {
                    rs.next();
                    reportedLevel = rs.getString(1);
                }

                Snapshot seenByA = readSnapshot(connA);
                System.out.printf(
                        "  A (SHOW transaction_isolation='%s') while B holds uncommitted +500.00 on Alice -> A sees Alice=%s, %d rows%n",
                        reportedLevel, seenByA.aliceBalance(), seenByA.rowCount());

                connB.rollback();
                bFinished = true;
                connA.commit();
                aFinished = true;
            } finally {
                try {
                    if (!bFinished) {
                        connB.rollback();
                    }
                } finally {
                    connB.setAutoCommit(true);
                }
                try {
                    if (!aFinished) {
                        connA.rollback();
                    }
                } finally {
                    connA.setAutoCommit(true);
                }
            }
        }
    }

    private static Snapshot readSnapshot(Connection conn) throws SQLException {
        BigDecimal balance;
        try (PreparedStatement ps = conn.prepareStatement(SQL_ALICE_BALANCE);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new IllegalStateException("Alice row not found");
            }
            balance = rs.getBigDecimal(1).setScale(2, RoundingMode.UNNECESSARY);
        }

        int count;
        try (PreparedStatement ps = conn.prepareStatement(SQL_ACCOUNT_COUNT);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            count = rs.getInt(1);
        }
        return new Snapshot(balance.toPlainString(), count);
    }

    private record Snapshot(String aliceBalance, int rowCount) {
    }
}
