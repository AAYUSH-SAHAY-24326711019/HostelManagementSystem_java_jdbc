package com.hostel.dao;

import com.hostel.model.Admin;
import com.hostel.util.DBConnection;
import com.hostel.util.PasswordUtil;

import java.sql.*;

public class AdminDAO {

    /** Authenticate admin. Returns the Admin object on success, null on failure. */
    public Admin authenticate(String username, String password) {
        String sql = "SELECT * FROM admin WHERE username = ? AND is_active = 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    String salt = rs.getString("salt");
                    if (PasswordUtil.verify(password, salt, storedHash)) {
                        Admin a = mapRow(rs);
                        updateLastLogin(a.getAdminId());
                        return a;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("AdminDAO.authenticate: " + e.getMessage());
        }
        return null;
    }

    public Admin getById(int adminId) {
        String sql = "SELECT * FROM admin WHERE admin_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("AdminDAO.getById: " + e.getMessage());
        }
        return null;
    }

    public boolean changePassword(int adminId, String newPassword) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(newPassword, salt);
        String sql = "UPDATE admin SET password_hash = ?, salt = ? WHERE admin_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, salt);
            ps.setInt(3, adminId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdminDAO.changePassword: " + e.getMessage());
            return false;
        }
    }

    public boolean updateProfile(Admin admin) {
        String sql = "UPDATE admin SET full_name=?, email=?, mobile=? WHERE admin_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, admin.getFullName());
            ps.setString(2, admin.getEmail());
            ps.setString(3, admin.getMobile());
            ps.setInt(4, admin.getAdminId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdminDAO.updateProfile: " + e.getMessage());
            return false;
        }
    }

    private void updateLastLogin(int adminId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE admin SET last_login = NOW() WHERE admin_id = ?")) {
            ps.setInt(1, adminId);
            ps.executeUpdate();
        } catch (SQLException ignored) { }
    }

    private Admin mapRow(ResultSet rs) throws SQLException {
        Admin a = new Admin();
        a.setAdminId(rs.getInt("admin_id"));
        a.setUsername(rs.getString("username"));
        a.setPasswordHash(rs.getString("password_hash"));
        a.setSalt(rs.getString("salt"));
        a.setFullName(rs.getString("full_name"));
        a.setEmail(rs.getString("email"));
        a.setMobile(rs.getString("mobile"));
        a.setActive(rs.getInt("is_active") == 1);
        a.setLastLogin(rs.getTimestamp("last_login"));
        return a;
    }
}
