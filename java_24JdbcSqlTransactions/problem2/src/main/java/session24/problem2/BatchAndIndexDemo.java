package session24.problem2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

public final class BatchAndIndexDemo {

    private static final int TOTAL_ROWS = 20_000;
    private static final int BATCH_SIZE = 1_000;
    private static final int WARMUP_ROWS = 2_000;

    private static final String SQL_INSERT =
            "INSERT INTO batch_item (code, qty) VALUES (?, ?)";
    private static final String SQL_VERIFY =
            "SELECT COUNT(*) AS cnt, COALESCE(SUM(qty), 0) AS sum_qty, COALESCE(MAX(id), 0) AS max_id FROM batch_item";

    private BatchAndIndexDemo() {
    }

    public static void main(String[] args) throws SQLException {
        // Untimed warm-up pass so JIT and driver initialization do not skew Variant 1
        warmUp();

        // 1. Row-by-row (connection opened outside timed window to match batched variants)
        truncateAndRestartIdentity();
        long rowByRowMs;
        try (Connection conn = DbSupport.openConnection()) {
            rowByRowMs = runRowByRow(conn);
        }
        printVerification("1. Row-by-row", rowByRowMs);

        // 2. Batched (flush every 1,000 rows)
        truncateAndRestartIdentity();
        BatchMetrics batchedMetrics;
        try (Connection conn = DbSupport.openConnection()) {
            batchedMetrics = runBatched(conn);
        }
        printBatchDetails("2. Batched (standard)", batchedMetrics);
        printVerification("2. Batched (standard)", batchedMetrics.elapsedMs());

        // 3. Batched with reWriteBatchedInserts=true
        truncateAndRestartIdentity();
        BatchMetrics rewriteMetrics;
        try (Connection conn = DbSupport.openConnectionWithRewriteBatchedInserts()) {
            rewriteMetrics = runBatched(conn);
        }
        printBatchDetails("3. Batched (reWriteBatchedInserts=true)", rewriteMetrics);
        printVerification("3. Batched (reWriteBatchedInserts=true)", rewriteMetrics.elapsedMs());

        // Summary ratios
        double ratioBatched = (double) rowByRowMs / Math.max(1L, batchedMetrics.elapsedMs());
        double ratioRewrite = (double) rowByRowMs / Math.max(1L, rewriteMetrics.elapsedMs());
        System.out.println("\n=== Timing Summary ===");
        System.out.printf("  Row-by-row elapsed:                      %d ms%n", rowByRowMs);
        System.out.printf("  Batched elapsed:                         %d ms%n", batchedMetrics.elapsedMs());
        System.out.printf("  Batched (reWriteBatchedInserts) elapsed: %d ms%n", rewriteMetrics.elapsedMs());
        System.out.printf("  Ratio (row-by-row / batched):            %.2fx%n", ratioBatched);
        System.out.printf("  Ratio (row-by-row / batched-rewrite):    %.2fx%n", ratioRewrite);
    }

    private static void warmUp() throws SQLException {
        truncateAndRestartIdentity();
        try (Connection conn = DbSupport.openConnection()) {
            conn.setAutoCommit(false);
            boolean committed = false;
            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
                for (int i = 1; i <= WARMUP_ROWS; i++) {
                    ps.setString(1, "WARMUP-" + i);
                    ps.setInt(2, i % 100);
                    ps.executeUpdate();
                }
                conn.commit();
                committed = true;
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

    private static long runRowByRow(Connection conn) throws SQLException {
        long startNanos = System.nanoTime();
        conn.setAutoCommit(false);
        boolean committed = false;
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            for (int i = 1; i <= TOTAL_ROWS; i++) {
                ps.setString(1, "CODE-" + i);
                ps.setInt(2, i % 100);
                ps.executeUpdate();
            }
            conn.commit();
            committed = true;
        } finally {
            try {
                if (!committed) {
                    conn.rollback();
                }
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private static BatchMetrics runBatched(Connection conn) throws SQLException {
        long startNanos = System.nanoTime();
        int firstBatchLength = 0;
        List<Integer> firstBatchDistinctValues = List.of();
        boolean capturedFirst = false;

        conn.setAutoCommit(false);
        boolean committed = false;
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            int queued = 0;
            for (int i = 1; i <= TOTAL_ROWS; i++) {
                ps.setString(1, "CODE-" + i);
                ps.setInt(2, i % 100);
                ps.addBatch();
                queued++;

                if (queued == BATCH_SIZE) {
                    int[] counts = ps.executeBatch();
                    if (!capturedFirst) {
                        firstBatchLength = counts.length;
                        firstBatchDistinctValues = Arrays.stream(counts).distinct().boxed().toList();
                        capturedFirst = true;
                    }
                    queued = 0;
                }
            }
            if (queued > 0) {
                ps.executeBatch();
            }
            conn.commit();
            committed = true;
        } finally {
            try {
                if (!committed) {
                    conn.rollback();
                }
            } finally {
                conn.setAutoCommit(true);
            }
        }

        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
        return new BatchMetrics(elapsedMs, firstBatchLength, firstBatchDistinctValues);
    }

    private static void printBatchDetails(String label, BatchMetrics metrics) {
        System.out.printf("=== %s ===%n", label);
        System.out.printf("  First executeBatch() array length:    %d%n", metrics.firstBatchLength());
        System.out.printf("  First executeBatch() distinct values: %s%n", metrics.firstBatchDistinctValues());
    }

    private static void printVerification(String label, long elapsedMs) throws SQLException {
        try (Connection conn = DbSupport.openConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_VERIFY);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            long count = rs.getLong("cnt");
            long sumQty = rs.getLong("sum_qty");
            long maxId = rs.getLong("max_id");
            System.out.printf("  [%s] elapsed=%d ms | count=%d, sum(qty)=%d, max(id)=%d%n",
                    label, elapsedMs, count, sumQty, maxId);
        }
    }

    private static void truncateAndRestartIdentity() throws SQLException {
        try (Connection conn = DbSupport.openConnection();
             Statement st = conn.createStatement()) {
            st.execute("TRUNCATE TABLE batch_item RESTART IDENTITY");
        }
    }

    private record BatchMetrics(long elapsedMs, int firstBatchLength, List<Integer> firstBatchDistinctValues) {
    }
}
