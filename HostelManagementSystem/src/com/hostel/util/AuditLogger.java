package com.hostel.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Writes to audit_log. Deliberately swallows all exceptions (logging must
 * never crash the calling business operation) — this is part of the
 * "fail safe" requirement for this project.
 */
public final class AuditLogger {

    private AuditLogger() { }

    public static void log(String userType, int userId, String action, String details) {
        String sql = "INSERT INTO audit_log (user_type, user_id, action, details) VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userType);
            ps.setInt(2, userId);
            ps.setString(3, action);
            ps.setString(4, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Never propagate — auditing is best-effort.
            System.err.println("AuditLogger: failed to write log entry: " + e.getMessage());
        }
    }
}
