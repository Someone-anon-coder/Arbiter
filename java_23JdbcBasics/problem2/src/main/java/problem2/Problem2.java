package problem2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Problem2 {
    public static void main(String[] args) throws Exception {
        String url = System.getenv("ARBITER_DB_URL");
        String user = System.getenv("ARBITER_DB_USER");
        String password = System.getenv("ARBITER_DB_PASSWORD");

        // FIX 4: Explicit null checks
        if (url == null) throw new IllegalStateException("Environment variable ARBITER_DB_URL is missing.");
        if (user == null) throw new IllegalStateException("Environment variable ARBITER_DB_USER is missing.");
        if (password == null) throw new IllegalStateException("Environment variable ARBITER_DB_PASSWORD is missing.");

        try (Connection c = DriverManager.getConnection(url, user, password)) {
            MemberDao dao = new MemberDao(c);
            
            dao.truncate();

            long idAda = dao.create(new Member(null, "ada@example.com", "Ada Lovelace", 36, "555-0101", 120, LocalDate.parse("2024-02-29")));
            long idGrace = dao.create(new Member(null, "grace@example.com", "Grace Hopper", 45, "555-0102", null, LocalDate.parse("2023-12-31")));
            long idLinus = dao.create(new Member(null, "linus@example.com", "Linus T", 28, null, 0, LocalDate.parse("2025-06-15")));
            System.out.printf("Created initial IDs: %d, %d, %d%n", idAda, idGrace, idLinus);

            tryFailingCreate(dao, "Duplicate", new Member(null, "ada@example.com", "Duplicate Ada", 30, null, 0, LocalDate.parse("2025-01-01")));
            tryFailingCreate(dao, "Age 12", new Member(null, "kid@example.com", "Kid", 12, null, 0, LocalDate.parse("2025-01-01")));
            tryFailingCreate(dao, "Null Name", new Member(null, "noname@example.com", null, 30, null, 0, LocalDate.parse("2025-01-01")));

            long idAlan = dao.create(new Member(null, "alan@example.com", "Alan Turing", 41, "555-0104", 75, LocalDate.parse("2025-01-01")));
            System.out.printf("Alan ID: %d%n", idAlan);

            System.out.println("findById(2): " + dao.findById(2));
            System.out.println("findById(3): " + dao.findById(3));
            System.out.println("findById(999): " + dao.findById(999));
            System.out.println("findByEmail(linus): " + dao.findByEmail("linus@example.com"));

            int updAda = dao.update(new Member(idAda, "ada@example.com", "Ada Lovelace", 36, null, 120, LocalDate.parse("2024-02-29")));
            int updAlan = dao.update(new Member(idAlan, "alan@example.com", "Alan Turing", 41, "555-0104", 90, LocalDate.parse("2025-01-01")));
            int upd999 = dao.update(new Member(999L, "x@example.com", "X", 30, null, 0, LocalDate.parse("2025-01-01")));
            System.out.printf("Update counts: %d, %d, %d%n", updAda, updAlan, upd999);

            System.out.println("delete(3) 1: " + dao.delete(3));
            System.out.println("delete(3) 2: " + dao.delete(3));
            System.out.println("findById(3) after delete: " + dao.findById(3));

            String injectionInput = "' OR '1'='1";
            try {
                int safeCount = dao.findByEmail(injectionInput).isPresent() ? 1 : 0;
                int vulnCount = dao.findByEmailVULNERABLE(injectionInput).size();
                System.out.printf("Injection Demo: vulnerable method %d rows, safe method %d rows%n", vulnCount, safeCount);
            } catch (SQLException e) {
                System.out.println("Injection query failed: " + e.getMessage());
            }

            System.out.println("--- FINAL REPORT ---");
            List<Member> all = dao.findAll();
            LocalDate referenceDate = LocalDate.of(2026, 1, 1);
            
            int totalMembers = all.size();
            int totalPoints = 0;
            int pointsCount = 0;
            int nullPointsCount = 0;
            int nullPhoneCount = 0;

            for (Member m : all) {
                String ptsDisplay = m.loyaltyPoints() == null ? "none" : m.loyaltyPoints().toString();
                String phoneDisplay = m.phone() == null ? "none" : m.phone();
                long days = ChronoUnit.DAYS.between(m.joinedOn(), referenceDate);
                
                System.out.printf("id %d has %s points, phone %s, %s, %d days%n", 
                        m.id(), ptsDisplay, phoneDisplay, m.joinedOn(), days);

                if (m.loyaltyPoints() == null) {
                    nullPointsCount++;
                } else {
                    totalPoints += m.loyaltyPoints();
                    pointsCount++;
                }
                
                if (m.phone() == null) {
                    nullPhoneCount++;
                }
            }

            double avgPoints = pointsCount > 0 ? (double) totalPoints / pointsCount : 0.0;
            System.out.printf("Aggregates: %d members, total points %d, average %.1f, %d member(s) with NULL points, %d with NULL phone%n",
                    totalMembers, totalPoints, avgPoints, nullPointsCount, nullPhoneCount);
        }
    }

    private static void tryFailingCreate(MemberDao dao, String label, Member m) {
        try {
            dao.create(m);
        } catch (MemberDataException e) {
            SQLException sqlEx = (SQLException) e.getCause();
            System.out.printf("Failure [%s]: Category: %s, SQLState: %s, Raw message: %s%n", 
                label, e.getCategory(), sqlEx.getSQLState(), sqlEx.getMessage());
        }
    }
}