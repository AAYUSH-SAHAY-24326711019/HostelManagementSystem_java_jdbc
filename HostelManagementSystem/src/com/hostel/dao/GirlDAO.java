package com.hostel.dao;

import com.hostel.model.Girl;
import com.hostel.util.DBConnection;
import com.hostel.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GirlDAO {

    // ---- Create -------------------------------------------------------
    public int insertGirl(Girl g) throws SQLException {
        String sql = "INSERT INTO girls (name,gender,age,dob,mobile,email,aadhar_number,college_name," +
                     "address,photo_path,room_id,plan_id,kitchen_plan_id,admission_date,status) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, g.getName());
            ps.setString(2, g.getGender());
            ps.setInt(3, g.getAge());
            ps.setDate(4, g.getDob());
            ps.setString(5, g.getMobile());
            ps.setString(6, g.getEmail());
            ps.setString(7, g.getAadharNumber());
            ps.setString(8, g.getCollegeAddress());
            ps.setString(9, g.getAddress());
            ps.setString(10, g.getPhotoPath());
            if (g.getRoomId() > 0) ps.setInt(11, g.getRoomId()); else ps.setNull(11, Types.INTEGER);
            if (g.getPlanId() > 0) ps.setInt(12, g.getPlanId()); else ps.setNull(12, Types.INTEGER);
            if (g.getKitchenPlanId() > 0) ps.setInt(13, g.getKitchenPlanId()); else ps.setNull(13, Types.INTEGER);
            ps.setDate(14, g.getAdmissionDate());
            ps.setString(15, "ACTIVE");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Insert failed, no generated key.");
    }

    // ---- Read ---------------------------------------------------------
    public Girl getById(int girlId) {
        String sql = "SELECT g.*, r.room_number, rp.plan_name, kp.plan_name AS kitchen_plan_name " +
                     "FROM girls g LEFT JOIN rooms r ON g.room_id=r.room_id " +
                     "LEFT JOIN room_plans rp ON g.plan_id=rp.plan_id " +
                     "LEFT JOIN kitchen_plans kp ON g.kitchen_plan_id=kp.kitchen_plan_id " +
                     "WHERE g.girl_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, girlId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { System.err.println("GirlDAO.getById: " + e.getMessage()); }
        return null;
    }

    public List<Girl> getAllActive() {
        return getByStatus("ACTIVE");
    }

    public List<Girl> getAllLeft() {
        return getByStatus("LEFT");
    }

    public List<Girl> getAll() {
        return queryList("SELECT g.*, r.room_number, rp.plan_name, kp.plan_name AS kitchen_plan_name " +
                "FROM girls g LEFT JOIN rooms r ON g.room_id=r.room_id " +
                "LEFT JOIN room_plans rp ON g.plan_id=rp.plan_id " +
                "LEFT JOIN kitchen_plans kp ON g.kitchen_plan_id=kp.kitchen_plan_id ORDER BY g.name");
    }

    private List<Girl> getByStatus(String status) {
        return queryList("SELECT g.*, r.room_number, rp.plan_name, kp.plan_name AS kitchen_plan_name " +
                "FROM girls g LEFT JOIN rooms r ON g.room_id=r.room_id " +
                "LEFT JOIN room_plans rp ON g.plan_id=rp.plan_id " +
                "LEFT JOIN kitchen_plans kp ON g.kitchen_plan_id=kp.kitchen_plan_id " +
                "WHERE g.status='" + status + "' ORDER BY g.name");
    }

    /** Girls who have unpaid bills due next month. */
    public List<Girl> getGirlsDueNextMonth() {
        String sql = "SELECT DISTINCT g.*, r.room_number, rp.plan_name, kp.plan_name AS kitchen_plan_name " +
                     "FROM girls g " +
                     "LEFT JOIN rooms r ON g.room_id=r.room_id " +
                     "LEFT JOIN room_plans rp ON g.plan_id=rp.plan_id " +
                     "LEFT JOIN kitchen_plans kp ON g.kitchen_plan_id=kp.kitchen_plan_id " +
                     "JOIN bills b ON g.girl_id=b.girl_id " +
                     "WHERE g.status='ACTIVE' AND b.status='UNPAID' " +
                     "AND MONTH(b.due_date)=MONTH(DATE_ADD(NOW(),INTERVAL 1 MONTH)) " +
                     "AND YEAR(b.due_date)=YEAR(DATE_ADD(NOW(),INTERVAL 1 MONTH))";
        return queryList(sql);
    }

    /** Girls who paid their bills this month. */
    public List<Girl> getGirlsPaidThisMonth() {
        String sql = "SELECT DISTINCT g.*, r.room_number, rp.plan_name, kp.plan_name AS kitchen_plan_name " +
                     "FROM girls g " +
                     "LEFT JOIN rooms r ON g.room_id=r.room_id " +
                     "LEFT JOIN room_plans rp ON g.plan_id=rp.plan_id " +
                     "LEFT JOIN kitchen_plans kp ON g.kitchen_plan_id=kp.kitchen_plan_id " +
                     "JOIN payments p ON g.girl_id=p.girl_id " +
                     "WHERE MONTH(p.payment_date)=MONTH(NOW()) AND YEAR(p.payment_date)=YEAR(NOW())";
        return queryList(sql);
    }

    /** Authenticate girl by username + password. */
    public Girl authenticateGirl(String username, String password) {
        String sql = "SELECT gc.*, g.girl_id, g.name FROM girl_credentials gc " +
                     "JOIN girls g ON gc.girl_id=g.girl_id WHERE gc.username=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    String salt = rs.getString("salt");
                    if (PasswordUtil.verify(password, salt, hash)) {
                        updateGirlLastLogin(rs.getInt("credential_id"), con);
                        return getById(rs.getInt("girl_id"));
                    }
                }
            }
        } catch (SQLException e) { System.err.println("GirlDAO.auth: " + e.getMessage()); }
        return null;
    }

    public boolean isTempPassword(int girlId) {
        String sql = "SELECT is_temp_password FROM girl_credentials WHERE girl_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, girlId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) == 1;
            }
        } catch (SQLException ignored) {}
        return false;
    }

    public boolean changeGirlPassword(int girlId, String newPassword) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(newPassword, salt);
        String sql = "UPDATE girl_credentials SET password_hash=?, salt=?, is_temp_password=0 WHERE girl_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash); ps.setString(2, salt); ps.setInt(3, girlId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    /** Creates login credentials for a girl. Returns the temp password for display. */
    public String createGirlCredentials(int girlId, String username) throws SQLException {
        String tempPwd = PasswordUtil.generateTempPassword();
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(tempPwd, salt);
        String sql = "INSERT INTO girl_credentials (girl_id, username, password_hash, salt, is_temp_password)" +
                     " VALUES (?,?,?,?,1) ON DUPLICATE KEY UPDATE username=?, password_hash=?, salt=?, is_temp_password=1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, girlId); ps.setString(2, username);
            ps.setString(3, hash); ps.setString(4, salt);
            ps.setString(5, username); ps.setString(6, hash); ps.setString(7, salt);
            ps.executeUpdate();
        }
        return tempPwd;
    }

    // ---- Update -------------------------------------------------------
    public boolean updateGirl(Girl g) {
        String sql = "UPDATE girls SET name=?,gender=?,age=?,dob=?,mobile=?,email=?,aadhar_number=?," +
                     "college_name=?,address=?,photo_path=?,room_id=?,plan_id=?,kitchen_plan_id=? WHERE girl_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, g.getName()); ps.setString(2, g.getGender()); ps.setInt(3, g.getAge());
            ps.setDate(4, g.getDob()); ps.setString(5, g.getMobile()); ps.setString(6, g.getEmail());
            ps.setString(7, g.getAadharNumber()); ps.setString(8, g.getCollegeAddress());
            ps.setString(9, g.getAddress()); ps.setString(10, g.getPhotoPath());
            if (g.getRoomId() > 0) ps.setInt(11, g.getRoomId()); else ps.setNull(11, Types.INTEGER);
            if (g.getPlanId() > 0) ps.setInt(12, g.getPlanId()); else ps.setNull(12, Types.INTEGER);
            if (g.getKitchenPlanId() > 0) ps.setInt(13, g.getKitchenPlanId()); else ps.setNull(13, Types.INTEGER);
            ps.setInt(14, g.getGirlId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("GirlDAO.update: " + e.getMessage()); return false; }
    }

    public boolean markGirlLeft(int girlId, java.sql.Date leavingDate) {
        String sql = "UPDATE girls SET status='LEFT', leaving_date=? WHERE girl_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, leavingDate); ps.setInt(2, girlId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // ---- Helpers ------------------------------------------------------
    private void updateGirlLastLogin(int credentialId, Connection con) {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE girl_credentials SET last_login=NOW() WHERE credential_id=?")) {
            ps.setInt(1, credentialId); ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private List<Girl> queryList(String sql) {
        List<Girl> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("GirlDAO.queryList: " + e.getMessage()); }
        return list;
    }

    private Girl mapRow(ResultSet rs) throws SQLException {
        Girl g = new Girl();
        g.setGirlId(rs.getInt("girl_id")); g.setName(rs.getString("name"));
        g.setGender(rs.getString("gender")); g.setAge(rs.getInt("age"));
        g.setDob(rs.getDate("dob")); g.setMobile(rs.getString("mobile"));
        g.setEmail(rs.getString("email")); g.setAadharNumber(rs.getString("aadhar_number"));
        g.setCollegeAddress(rs.getString("college_name")); g.setAddress(rs.getString("address"));
        g.setPhotoPath(rs.getString("photo_path"));
        g.setRoomId(rs.getInt("room_id")); g.setPlanId(rs.getInt("plan_id"));
        g.setKitchenPlanId(rs.getInt("kitchen_plan_id"));
        g.setAdmissionDate(rs.getDate("admission_date")); g.setLeavingDate(rs.getDate("leaving_date"));
        g.setStatus(rs.getString("status"));
        try { g.setRoomNumber(rs.getString("room_number")); } catch (SQLException ignored) {}
        try { g.setPlanName(rs.getString("plan_name")); } catch (SQLException ignored) {}
        try { g.setKitchenPlanName(rs.getString("kitchen_plan_name")); } catch (SQLException ignored) {}
        return g;
    }
}
