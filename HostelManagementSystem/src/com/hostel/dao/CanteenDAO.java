package com.hostel.dao;

import com.hostel.model.CanteenServiceLog;
import com.hostel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CanteenDAO {

    /**
     * Marks today's canteen status.
     * If status is NOT_RENDERED and a kitchen plan exists, creates refund rows for all active girls.
     */
    public boolean markService(java.sql.Date serviceDate, String status, String remarks, int adminId) {
        String sql = "INSERT INTO canteen_service_log (service_date,status,remarks,marked_by) VALUES (?,?,?,?) " +
                     "ON DUPLICATE KEY UPDATE status=?, remarks=?, marked_by=?, marked_at=NOW()";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, serviceDate); ps.setString(2, status);
            ps.setString(3, remarks); ps.setInt(4, adminId);
            ps.setString(5, status); ps.setString(6, remarks); ps.setInt(7, adminId);
            ps.executeUpdate();

            if ("NOT_RENDERED".equals(status)) {
                int logId = -1;
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT log_id FROM canteen_service_log WHERE service_date='" + serviceDate + "'")) {
                    if (rs.next()) logId = rs.getInt(1);
                }
                if (logId > 0) generateRefunds(logId, serviceDate, con);
            }
            return true;
        } catch (SQLException e) { System.err.println("CanteenDAO.mark: " + e.getMessage()); return false; }
    }

    private void generateRefunds(int logId, java.sql.Date serviceDate, Connection con) {
        // Get daily refund amount per girl from their kitchen plan
        String sql = "SELECT g.girl_id, kp.monthly_charge/30 AS daily_refund " +
                     "FROM girls g JOIN kitchen_plans kp ON g.kitchen_plan_id=kp.kitchen_plan_id " +
                     "WHERE g.status='ACTIVE' AND kp.is_refundable=1";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            try (PreparedStatement ins = con.prepareStatement(
                    "INSERT IGNORE INTO canteen_refunds (girl_id,log_id,refund_amount,refund_date,status) VALUES (?,?,?,?,'PENDING')")) {
                while (rs.next()) {
                    ins.setInt(1, rs.getInt("girl_id")); ins.setInt(2, logId);
                    ins.setDouble(3, Math.round(rs.getDouble("daily_refund") * 100.0) / 100.0);
                    ins.setDate(4, serviceDate); ins.addBatch();
                }
                ins.executeBatch();
            }
        } catch (SQLException e) { System.err.println("CanteenDAO.generateRefunds: " + e.getMessage()); }
    }

    public boolean markRefundPaid(int girlId, int logId) {
        String sql = "UPDATE canteen_refunds SET status='PAID' WHERE girl_id=? AND log_id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, girlId); ps.setInt(2, logId); return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public List<CanteenServiceLog> getServiceLogs() {
        List<CanteenServiceLog> list = new ArrayList<>();
        String sql = "SELECT * FROM canteen_service_log ORDER BY service_date DESC";
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapLog(rs));
        } catch (SQLException e) { System.err.println("CanteenDAO.getLogs: " + e.getMessage()); }
        return list;
    }

    /** Girls with pending canteen refunds (for the refund list panel). */
    public List<Object[]> getPendingRefunds() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT g.name, g.girl_id, cr.refund_id, cr.refund_amount, cr.refund_date, csl.service_date " +
                     "FROM canteen_refunds cr " +
                     "JOIN girls g ON cr.girl_id=g.girl_id " +
                     "JOIN canteen_service_log csl ON cr.log_id=csl.log_id " +
                     "WHERE cr.status='PENDING' ORDER BY csl.service_date";
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("name"), rs.getInt("girl_id"), rs.getInt("refund_id"),
                    rs.getDouble("refund_amount"), rs.getDate("service_date")
                });
            }
        } catch (SQLException e) { System.err.println("CanteenDAO.pendingRefunds: " + e.getMessage()); }
        return list;
    }

    public CanteenServiceLog getLogByDate(java.sql.Date date) {
        String sql = "SELECT * FROM canteen_service_log WHERE service_date=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, date);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapLog(rs); }
        } catch (SQLException e) { System.err.println("CanteenDAO.getByDate: " + e.getMessage()); }
        return null;
    }

    private CanteenServiceLog mapLog(ResultSet rs) throws SQLException {
        CanteenServiceLog log = new CanteenServiceLog();
        log.setLogId(rs.getInt("log_id")); log.setServiceDate(rs.getDate("service_date"));
        log.setStatus(rs.getString("status")); log.setRemarks(rs.getString("remarks"));
        log.setMarkedBy(rs.getInt("marked_by")); log.setMarkedAt(rs.getTimestamp("marked_at"));
        return log;
    }
}
