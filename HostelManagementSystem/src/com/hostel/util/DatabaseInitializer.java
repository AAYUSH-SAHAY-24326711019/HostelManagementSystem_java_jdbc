package com.hostel.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Runs once at application startup:
 *  1. Verifies the schema exists (friendly error if the SQL script was never run).
 *  2. If the default admin row still has the SQL-script placeholder hash,
 *     replaces it with a real salted hash of the documented default password,
 *     so the very first login always works without manual SQL editing.
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() { }

    /** @return null if everything is fine, otherwise a human-readable error message. */
    public static String runStartupChecks() {
        if (!DBConnection.testConnection()) {
            return "Could not connect to MySQL.\n\n"
                 + "Please check:\n"
                 + "  1. MySQL server is running.\n"
                 + "  2. db.properties (in the project root) has the correct host/user/password.\n"
                 + "  3. You have imported sql/hostel_management.sql into MySQL Workbench.\n\n"
                 + "JDBC URL attempted: " + safeUrl();
        }

        try (Connection con = DBConnection.getConnection()) {
            ensureSchemaPresent(con);
            ensureDefaultAdmin(con);
        } catch (SQLException e) {
            return "Database schema check failed: " + e.getMessage()
                 + "\n\nMake sure sql/hostel_management.sql was imported completely.";
        }
        return null;
    }

    private static String safeUrl() {
        try {
            return DBConnection.getJdbcUrl();
        } catch (Exception e) {
            return "(unavailable)";
        }
    }

    private static void ensureSchemaPresent(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeQuery("SELECT 1 FROM admin LIMIT 1");
        }
    }

    private static void ensureDefaultAdmin(Connection con) throws SQLException {
        String check = "SELECT admin_id, password_hash FROM admin WHERE username = ?";
        try (PreparedStatement ps = con.prepareStatement(check)) {
            ps.setString(1, AppConstants.DEFAULT_ADMIN_USERNAME);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    if ("PLACEHOLDER_HASH".equals(hash)) {
                        String salt = PasswordUtil.generateSalt();
                        String realHash = PasswordUtil.hash(AppConstants.DEFAULT_ADMIN_PASSWORD, salt);
                        String update = "UPDATE admin SET password_hash=?, salt=? WHERE username=?";
                        try (PreparedStatement up = con.prepareStatement(update)) {
                            up.setString(1, realHash);
                            up.setString(2, salt);
                            up.setString(3, AppConstants.DEFAULT_ADMIN_USERNAME);
                            up.executeUpdate();
                        }
                    }
                }
            }
        }
    }
}
