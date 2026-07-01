package com.hostel.dao;

import com.hostel.model.*;
import com.hostel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {

    public boolean addFine(Fine f) {
        String sql = "INSERT INTO fines (girl_id,reason,amount,fine_date,status,imposed_by) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, f.getGirlId()); ps.setString(2, f.getReason()); ps.setDouble(3, f.getAmount());
            ps.setDate(4, f.getFineDate()); ps.setString(5, "UNPAID");
            if (f.getImposedBy() > 0) ps.setInt(6, f.getImposedBy()); else ps.setNull(6, Types.INTEGER);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("FineDAO.add: " + e.getMessage()); return false; }
    }

    public boolean markPaid(int fineId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE fines SET status='PAID' WHERE fine_id=?")) {
            ps.setInt(1, fineId); return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public List<Fine> getFinesByGirlId(int girlId) {
        List<Fine> list = new ArrayList<>();
        String sql = "SELECT f.*, g.name AS girl_name FROM fines f JOIN girls g ON f.girl_id=g.girl_id WHERE f.girl_id=? ORDER BY f.fine_date DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, girlId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapFine(rs)); }
        } catch (SQLException e) { System.err.println("FineDAO.getByGirl: " + e.getMessage()); }
        return list;
    }

    public List<Fine> getAllFines() {
        List<Fine> list = new ArrayList<>();
        String sql = "SELECT f.*, g.name AS girl_name FROM fines f JOIN girls g ON f.girl_id=g.girl_id ORDER BY f.fine_date DESC";
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapFine(rs));
        } catch (SQLException e) { System.err.println("FineDAO.getAll: " + e.getMessage()); }
        return list;
    }

    private Fine mapFine(ResultSet rs) throws SQLException {
        Fine f = new Fine();
        f.setFineId(rs.getInt("fine_id")); f.setGirlId(rs.getInt("girl_id"));
        f.setReason(rs.getString("reason")); f.setAmount(rs.getDouble("amount"));
        f.setFineDate(rs.getDate("fine_date")); f.setStatus(rs.getString("status"));
        try { f.setGirlName(rs.getString("girl_name")); } catch (Exception ignored) {}
        return f;
    }
}
