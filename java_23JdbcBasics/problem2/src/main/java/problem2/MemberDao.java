package problem2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberDao {
    private final Connection c;

    public MemberDao(Connection c) {
        this.c = c;
    }

    public void truncate() throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("TRUNCATE TABLE member RESTART IDENTITY")) {
            ps.executeUpdate();
        }
    }

    public long create(Member m) throws MemberDataException {
        String sql = "INSERT INTO member (email, full_name, age, phone, loyalty_points, joined_on) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.email());
            ps.setString(2, m.fullName());
            ps.setInt(3, m.age());
            
            // MINOR IMPROVEMENT: Explicit setNull portable form
            if (m.phone() == null) {
                ps.setNull(4, java.sql.Types.VARCHAR);
            } else {
                ps.setString(4, m.phone());
            }

            if (m.loyaltyPoints() == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, m.loyaltyPoints());
            }

            ps.setObject(6, m.joinedOn());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
                throw new SQLException("Failed to return generated key.");
            }
        } catch (SQLException e) {
            throw translateException(e);
        }
    }

    public Optional<Member> findById(long id) throws MemberDataException {
        String sql = "SELECT * FROM member WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw translateException(e);
        }
    }

    public Optional<Member> findByEmail(String email) throws MemberDataException {
        String sql = "SELECT * FROM member WHERE email = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw translateException(e);
        }
    }
    
    public List<Member> findByEmailVULNERABLE(String email) throws SQLException {
        List<Member> results = new ArrayList<>();
        String sql = "SELECT * FROM member WHERE email = '" + email + "'";
        try (Statement st = c.createStatement(); 
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) results.add(mapRow(rs));
        }
        return results;
    }

    public List<Member> findAll() throws MemberDataException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM member ORDER BY id";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) members.add(mapRow(rs));
        } catch (SQLException e) {
            throw translateException(e);
        }
        return members;
    }

    public int update(Member m) throws MemberDataException {
        String sql = "UPDATE member SET phone = ?, loyalty_points = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            // MINOR IMPROVEMENT: Explicit setNull portable form
            if (m.phone() == null) {
                ps.setNull(1, java.sql.Types.VARCHAR);
            } else {
                ps.setString(1, m.phone());
            }

            if (m.loyaltyPoints() == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, m.loyaltyPoints());
            }

            ps.setLong(3, m.id());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw translateException(e);
        }
    }

    public boolean delete(long id) throws MemberDataException {
        String sql = "DELETE FROM member WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw translateException(e);
        }
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        return new Member(
            rs.getLong("id"),
            rs.getString("email"),
            rs.getString("full_name"),
            rs.getInt("age"),
            rs.getString("phone"),
            rs.getObject("loyalty_points", Integer.class),
            rs.getObject("joined_on", LocalDate.class)
        );
    }

    private MemberDataException translateException(SQLException e) {
        String state = e.getSQLState();
        ErrorCategory category;
        
        if (state == null) {
            category = ErrorCategory.UNEXPECTED;
        } else if (state.equals("23505")) {
            category = ErrorCategory.DUPLICATE_KEY;
        } else if (state.startsWith("23")) {
            category = ErrorCategory.CONSTRAINT_VIOLATION;
        } else {
            category = ErrorCategory.UNEXPECTED;
        }
        
        return new MemberDataException(category, "Database operation failed.", e);
    }
}