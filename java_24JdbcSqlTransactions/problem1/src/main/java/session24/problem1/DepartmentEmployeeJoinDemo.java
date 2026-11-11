package session24.problem1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DepartmentEmployeeJoinDemo {

    private static final String SQL_INNER_JOIN = """
            SELECT e.full_name, d.name AS department_name
            FROM employee e
            INNER JOIN department d ON e.department_id = d.id
            ORDER BY e.id
            """;

    private static final String SQL_LEFT_JOIN_DEPT_LEFT = """
            SELECT d.name AS department_name, e.full_name
            FROM department d
            LEFT JOIN employee e ON d.id = e.department_id
            ORDER BY d.id, e.id
            """;

    private static final String SQL_LEFT_JOIN_EMP_LEFT = """
            SELECT e.full_name, d.name AS department_name
            FROM employee e
            LEFT JOIN department d ON e.department_id = d.id
            ORDER BY e.id
            """;

    private static final String SQL_GROUP_BY_DEPT = """
            SELECT d.name AS department_name, COUNT(e.id) AS employee_count
            FROM department d
            LEFT JOIN employee e ON d.id = e.department_id
            GROUP BY d.id, d.name
            ORDER BY d.id
            """;

    private DepartmentEmployeeJoinDemo() {
    }

    public static void main(String[] args) throws SQLException {
        try (Connection conn = openConnection()) {
            runInnerJoin(conn);
            System.out.println();
            runLeftJoinDepartmentLeft(conn);
            System.out.println();
            runLeftJoinEmployeeLeft(conn);
            System.out.println();
            runGroupByReport(conn);
        }
    }

    private static Connection openConnection() throws SQLException {
        String url = requireEnv("ARBITER_DB_URL");
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

    private static void runInnerJoin(Connection conn) throws SQLException {
        System.out.println("=== 1. INNER JOIN (employee -> department, ordered by e.id) ===");
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(SQL_INNER_JOIN);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String empName = rs.getString("full_name");
                String deptName = rs.getString("department_name");
                System.out.printf("  %s | %s%n", empName, deptName);
                count++;
            }
        }
        System.out.println("Row count: " + count);
    }

    private static void runLeftJoinDepartmentLeft(Connection conn) throws SQLException {
        System.out.println("=== 2. LEFT JOIN (department on left, ordered by d.id, e.id) ===");
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(SQL_LEFT_JOIN_DEPT_LEFT);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String deptName = rs.getString("department_name");
                String empName = rs.getString("full_name");
                System.out.printf("  %s | %s%n", deptName, empName);
                count++;
            }
        }
        System.out.println("Row count: " + count);
    }

    private static void runLeftJoinEmployeeLeft(Connection conn) throws SQLException {
        System.out.println("=== 3. LEFT JOIN (employee on left, ordered by e.id) ===");
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(SQL_LEFT_JOIN_EMP_LEFT);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String empName = rs.getString("full_name");
                String deptName = rs.getString("department_name");
                System.out.printf("  %s | %s%n", empName, deptName);
                count++;
            }
        }
        System.out.println("Row count: " + count);
    }

    private static void runGroupByReport(Connection conn) throws SQLException {
        System.out.println("=== 4. GROUP BY department (ordered by d.id) ===");
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(SQL_GROUP_BY_DEPT);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String deptName = rs.getString("department_name");
                long empCount = rs.getLong("employee_count");
                System.out.printf("  %s | %d%n", deptName, empCount);
                count++;
            }
        }
        System.out.println("Row count: " + count);
    }
}
