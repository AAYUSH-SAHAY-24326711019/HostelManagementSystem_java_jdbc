package com.hostel.dao;

import com.hostel.model.Complaint;
import com.hostel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDAO {

    public boolean fileComplaint(Complaint c) {
        String sql = "INSERT INTO complaints (girl_id,subject,description,status) VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.getGirlId()); ps.setString(2, c.getSubject());
            ps.setString(3, c.getDescription()); ps.setString(4, "OPEN");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("ComplaintDAO.file: " + e.getMessage()); return false; }
    }

    public boolean updateStatus(int complaintId, String status, String adminRemarks) {
        String sql = "UPDATE complaints SET status=?, admin_remarks=?, resolved_date=? WHERE complaint_id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status); ps.setString(2, adminRemarks);
            ps.setTimestamp(3, "RESOLVED".equals(status) ? new Timestamp(System.currentTimeMillis()) : null);
            ps.setInt(4, complaintId); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("ComplaintDAO.update: " + e.getMessage()); return false; }
    }

    public List<Complaint> getAllComplaints() {
        List<Complaint> list = new ArrayList<>();
        String sql = "SELECT c.*, g.name AS girl_name FROM complaints c JOIN girls g ON c.girl_id=g.girl_id ORDER BY c.filed_date DESC";
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("ComplaintDAO.getAll: " + e.getMessage()); }
        return list;
    }

    public List<Complaint> getComplaintsByGirlId(int girlId) {
        List<Complaint> list = new ArrayList<>();
        String sql = "SELECT c.*, g.name AS girl_name FROM complaints c JOIN girls g ON c.girl_id=g.girl_id WHERE c.girl_id=? ORDER BY c.filed_date DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, girlId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRow(rs)); }
        } catch (SQLException e) { System.err.println("ComplaintDAO.byGirl: " + e.getMessage()); }
        return list;
    }

    private Complaint mapRow(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setComplaintId(rs.getInt("complaint_id")); c.setGirlId(rs.getInt("girl_id"));
        c.setSubject(rs.getString("subject")); c.setDescription(rs.getString("description"));
        c.setStatus(rs.getString("status")); c.setAdminRemarks(rs.getString("admin_remarks"));
        c.setFiledDate(rs.getTimestamp("filed_date")); c.setResolvedDate(rs.getTimestamp("resolved_date"));
        try { c.setGirlName(rs.getString("girl_name")); } catch (Exception ignored) {}
        return c;
    }
}
