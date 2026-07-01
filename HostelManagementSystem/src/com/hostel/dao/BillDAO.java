package com.hostel.dao;

import com.hostel.model.Bill;
import com.hostel.model.Payment;
import com.hostel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    public int createBill(Bill b) throws SQLException {
        String sql = "INSERT INTO bills (girl_id,bill_type,bill_month,bill_year,amount,due_date,status,generated_date,generated_by,remarks) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getGirlId()); ps.setString(2, b.getBillType());
            if (b.getBillMonth() != null) ps.setInt(3, b.getBillMonth()); else ps.setNull(3, Types.INTEGER);
            if (b.getBillYear() != null) ps.setInt(4, b.getBillYear()); else ps.setNull(4, Types.INTEGER);
            ps.setDouble(5, b.getAmount()); ps.setDate(6, b.getDueDate());
            ps.setString(7, "UNPAID"); ps.setDate(8, new Date(System.currentTimeMillis()));
            if (b.getGeneratedBy() > 0) ps.setInt(9, b.getGeneratedBy()); else ps.setNull(9, Types.INTEGER);
            ps.setString(10, b.getRemarks());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int billId = rs.getInt(1);
                    // Automatically create a due record
                    insertDue(billId, b.getGirlId(), b.getAmount(), b.getDueDate(), con);
                    return billId;
                }
            }
        }
        throw new SQLException("Bill creation failed.");
    }

    private void insertDue(int billId, int girlId, double amount, Date dueDate, Connection con) {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO dues (girl_id,bill_id,amount_due,due_date,status) VALUES (?,?,?,?,?)")) {
            ps.setInt(1, girlId); ps.setInt(2, billId); ps.setDouble(3, amount);
            ps.setDate(4, dueDate); ps.setString(5, "PENDING");
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("BillDAO.insertDue: " + e.getMessage()); }
    }

    public List<Bill> getBillsByGirlId(int girlId) {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT b.*, g.name AS girl_name FROM bills b JOIN girls g ON b.girl_id=g.girl_id WHERE b.girl_id=? ORDER BY b.generated_date DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, girlId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapBill(rs)); }
        } catch (SQLException e) { System.err.println("BillDAO.getByGirl: " + e.getMessage()); }
        return list;
    }

    public List<Bill> getAllUnpaidBills() {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT b.*, g.name AS girl_name FROM bills b JOIN girls g ON b.girl_id=g.girl_id WHERE b.status='UNPAID' ORDER BY b.due_date";
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapBill(rs));
        } catch (SQLException e) { System.err.println("BillDAO.allUnpaid: " + e.getMessage()); }
        return list;
    }

    public int recordPayment(Payment p) throws SQLException {
        String sql = "INSERT INTO payments (bill_id,girl_id,amount_paid,payment_date,payment_mode,received_by,receipt_no) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getBillId()); ps.setInt(2, p.getGirlId()); ps.setDouble(3, p.getAmountPaid());
            ps.setDate(4, p.getPaymentDate()); ps.setString(5, p.getPaymentMode());
            if (p.getReceivedBy() > 0) ps.setInt(6, p.getReceivedBy()); else ps.setNull(6, Types.INTEGER);
            String receipt = "RCP-" + System.currentTimeMillis();
            ps.setString(7, receipt); ps.executeUpdate();
            // Update bill status
            updateBillStatus(p.getBillId(), p.getAmountPaid(), con);
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Payment recording failed.");
    }

    private void updateBillStatus(int billId, double amountPaid, Connection con) {
        try {
            double billAmount = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT amount FROM bills WHERE bill_id=?")) {
                ps.setInt(1, billId);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) billAmount = rs.getDouble(1); }
            }
            double totalPaid = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT SUM(amount_paid) FROM payments WHERE bill_id=?")) {
                ps.setInt(1, billId);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) totalPaid = rs.getDouble(1); }
            }
            String newStatus = totalPaid >= billAmount ? "PAID" : "PARTIAL";
            try (PreparedStatement ps = con.prepareStatement("UPDATE bills SET status=? WHERE bill_id=?")) {
                ps.setString(1, newStatus); ps.setInt(2, billId); ps.executeUpdate();
            }
            if ("PAID".equals(newStatus)) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE dues SET status='CLEARED', cleared_date=CURDATE() WHERE bill_id=?")) {
                    ps.setInt(1, billId); ps.executeUpdate();
                }
            } else {
                double remaining = billAmount - totalPaid;
                try (PreparedStatement ps = con.prepareStatement("UPDATE dues SET amount_due=? WHERE bill_id=?")) {
                    ps.setDouble(1, remaining); ps.setInt(2, billId); ps.executeUpdate();
                }
            }
        } catch (SQLException e) { System.err.println("BillDAO.updateStatus: " + e.getMessage()); }
    }

    public List<Payment> getPaymentsByGirlId(int girlId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, g.name AS girl_name, b.bill_type FROM payments p " +
                     "JOIN girls g ON p.girl_id=g.girl_id JOIN bills b ON p.bill_id=b.bill_id " +
                     "WHERE p.girl_id=? ORDER BY p.payment_date DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, girlId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapPayment(rs)); }
        } catch (SQLException e) { System.err.println("BillDAO.getPaymentsByGirl: " + e.getMessage()); }
        return list;
    }

    public List<com.hostel.model.Due> getDuesByGirlId(int girlId) {
        List<com.hostel.model.Due> list = new ArrayList<>();
        String sql = "SELECT d.*, g.name AS girl_name, b.bill_type FROM dues d " +
                     "JOIN girls g ON d.girl_id=g.girl_id JOIN bills b ON d.bill_id=b.bill_id " +
                     "WHERE d.girl_id=? AND d.status='PENDING' ORDER BY d.due_date";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, girlId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) { list.add(mapDue(rs)); } }
        } catch (SQLException e) { System.err.println("BillDAO.getDuesByGirl: " + e.getMessage()); }
        return list;
    }

    public List<com.hostel.model.Due> getAllPendingDues() {
        List<com.hostel.model.Due> list = new ArrayList<>();
        String sql = "SELECT d.*, g.name AS girl_name, b.bill_type FROM dues d " +
                     "JOIN girls g ON d.girl_id=g.girl_id JOIN bills b ON d.bill_id=b.bill_id " +
                     "WHERE d.status='PENDING' ORDER BY d.due_date";
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapDue(rs));
        } catch (SQLException e) { System.err.println("BillDAO.allDues: " + e.getMessage()); }
        return list;
    }

    private Bill mapBill(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setBillId(rs.getInt("bill_id")); b.setGirlId(rs.getInt("girl_id"));
        b.setBillType(rs.getString("bill_type")); b.setAmount(rs.getDouble("amount"));
        b.setDueDate(rs.getDate("due_date")); b.setStatus(rs.getString("status"));
        b.setGeneratedDate(rs.getDate("generated_date")); b.setRemarks(rs.getString("remarks"));
        try { b.setBillMonth((Integer) rs.getObject("bill_month")); } catch (Exception ignored) {}
        try { b.setBillYear((Integer) rs.getObject("bill_year")); } catch (Exception ignored) {}
        try { b.setGirlName(rs.getString("girl_name")); } catch (Exception ignored) {}
        return b;
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id")); p.setBillId(rs.getInt("bill_id"));
        p.setGirlId(rs.getInt("girl_id")); p.setAmountPaid(rs.getDouble("amount_paid"));
        p.setPaymentDate(rs.getDate("payment_date")); p.setPaymentMode(rs.getString("payment_mode"));
        p.setReceiptNo(rs.getString("receipt_no"));
        try { p.setGirlName(rs.getString("girl_name")); } catch (Exception ignored) {}
        try { p.setBillType(rs.getString("bill_type")); } catch (Exception ignored) {}
        return p;
    }

    private com.hostel.model.Due mapDue(ResultSet rs) throws SQLException {
        com.hostel.model.Due d = new com.hostel.model.Due();
        d.setDueId(rs.getInt("due_id")); d.setGirlId(rs.getInt("girl_id"));
        d.setBillId(rs.getInt("bill_id")); d.setAmountDue(rs.getDouble("amount_due"));
        d.setDueDate(rs.getDate("due_date")); d.setStatus(rs.getString("status"));
        try { d.setGirlName(rs.getString("girl_name")); } catch (Exception ignored) {}
        try { d.setBillType(rs.getString("bill_type")); } catch (Exception ignored) {}
        return d;
    }
}
