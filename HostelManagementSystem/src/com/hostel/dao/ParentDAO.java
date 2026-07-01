package com.hostel.dao;

import com.hostel.model.Girl;
import com.hostel.model.Parent;
import com.hostel.util.DBConnection;
import com.hostel.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParentDAO {

    public int insertParent(Parent p) throws SQLException {
        String sql = "INSERT INTO parents (girl_id,relation_type,name,mobile,email,aadhar_number,occupation,address) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getGirlId()); ps.setString(2, p.getRelationType());
            ps.setString(3, p.getName()); ps.setString(4, p.getMobile());
            ps.setString(5, p.getEmail()); ps.setString(6, p.getAadharNumber());
            ps.setString(7, p.getOccupation()); ps.setString(8, p.getAddress());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Insert parent failed.");
    }

    public List<Parent> getByGirlId(int girlId) {
        List<Parent> list = new ArrayList<>();
        String sql = "SELECT * FROM parents WHERE girl_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, girlId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("ParentDAO.getByGirlId: " + e.getMessage()); }
        return list;
    }

    public boolean updateParent(Parent p) {
        String sql = "UPDATE parents SET relation_type=?,name=?,mobile=?,email=?,aadhar_number=?,occupation=?,address=? WHERE parent_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getRelationType()); ps.setString(2, p.getName());
            ps.setString(3, p.getMobile()); ps.setString(4, p.getEmail());
            ps.setString(5, p.getAadharNumber()); ps.setString(6, p.getOccupation());
            ps.setString(7, p.getAddress()); ps.setInt(8, p.getParentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    /** Creates parent login credentials. Returns temp password. */
    public String createParentCredentials(int parentId, int girlId, String username) throws SQLException {
        String tempPwd = PasswordUtil.generateTempPassword();
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(tempPwd, salt);
        String sql = "INSERT INTO parent_credentials (parent_id,girl_id,username,password_hash,salt,is_temp_password) " +
                     "VALUES (?,?,?,?,?,1) ON DUPLICATE KEY UPDATE username=?,password_hash=?,salt=?,is_temp_password=1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, parentId); ps.setInt(2, girlId); ps.setString(3, username);
            ps.setString(4, hash); ps.setString(5, salt);
            ps.setString(6, username); ps.setString(7, hash); ps.setString(8, salt);
            ps.executeUpdate();
        }
        return tempPwd;
    }

    /** Authenticate a parent. Returns [parentId, girlId] or null. */
    public int[] authenticateParent(String username, String password) {
        String sql = "SELECT pc.*, p.parent_id FROM parent_credentials pc " +
                     "JOIN parents p ON pc.parent_id=p.parent_id WHERE pc.username=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (PasswordUtil.verify(password, rs.getString("salt"), rs.getString("password_hash"))) {
                        // update last login
                        try (PreparedStatement up = con.prepareStatement(
                                "UPDATE parent_credentials SET last_login=NOW() WHERE credential_id=?")) {
                            up.setInt(1, rs.getInt("credential_id")); up.executeUpdate();
                        }
                        return new int[]{ rs.getInt("parent_id"), rs.getInt("girl_id") };
                    }
                }
            }
        } catch (SQLException e) { System.err.println("ParentDAO.auth: " + e.getMessage()); }
        return null;
    }

    public boolean isTempPassword(int parentId) {
        String sql = "SELECT is_temp_password FROM parent_credentials WHERE parent_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) == 1;
            }
        } catch (SQLException ignored) {}
        return false;
    }

    public boolean changeParentPassword(int parentId, String newPassword) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(newPassword, salt);
        String sql = "UPDATE parent_credentials SET password_hash=?,salt=?,is_temp_password=0 WHERE parent_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash); ps.setString(2, salt); ps.setInt(3, parentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    private Parent mapRow(ResultSet rs) throws SQLException {
        Parent p = new Parent();
        p.setParentId(rs.getInt("parent_id")); p.setGirlId(rs.getInt("girl_id"));
        p.setRelationType(rs.getString("relation_type")); p.setName(rs.getString("name"));
        p.setMobile(rs.getString("mobile")); p.setEmail(rs.getString("email"));
        p.setAadharNumber(rs.getString("aadhar_number")); p.setOccupation(rs.getString("occupation"));
        p.setAddress(rs.getString("address"));
        return p;
    }
}
