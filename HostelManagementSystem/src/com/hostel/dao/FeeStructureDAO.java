package com.hostel.dao;

import com.hostel.model.FeeStructure;
import com.hostel.util.DBConnection;

import java.sql.*;

public class FeeStructureDAO {

    public void saveOrUpdate(FeeStructure fs) throws SQLException {
        String sql = "INSERT INTO fee_structure (girl_id,monthly_stay_bill,emergency_deposit,electricity_deposit," +
                     "wifi_deposit,plan_extra_charge,kitchen_charge,effective_date) VALUES (?,?,?,?,?,?,?,?) " +
                     "ON DUPLICATE KEY UPDATE monthly_stay_bill=?,emergency_deposit=?,electricity_deposit=?," +
                     "wifi_deposit=?,plan_extra_charge=?,kitchen_charge=?,effective_date=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, fs.getGirlId());
            ps.setDouble(2, fs.getMonthlyStayBill()); ps.setDouble(3, fs.getEmergencyDeposit());
            ps.setDouble(4, fs.getElectricityDeposit()); ps.setDouble(5, fs.getWifiDeposit());
            ps.setDouble(6, fs.getPlanExtraCharge()); ps.setDouble(7, fs.getKitchenCharge());
            ps.setDate(8, fs.getEffectiveDate());
            ps.setDouble(9, fs.getMonthlyStayBill()); ps.setDouble(10, fs.getEmergencyDeposit());
            ps.setDouble(11, fs.getElectricityDeposit()); ps.setDouble(12, fs.getWifiDeposit());
            ps.setDouble(13, fs.getPlanExtraCharge()); ps.setDouble(14, fs.getKitchenCharge());
            ps.setDate(15, fs.getEffectiveDate());
            ps.executeUpdate();
        }
    }

    public FeeStructure getByGirlId(int girlId) {
        String sql = "SELECT * FROM fee_structure WHERE girl_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, girlId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    FeeStructure fs = new FeeStructure();
                    fs.setFeeId(rs.getInt("fee_id")); fs.setGirlId(rs.getInt("girl_id"));
                    fs.setMonthlyStayBill(rs.getDouble("monthly_stay_bill"));
                    fs.setEmergencyDeposit(rs.getDouble("emergency_deposit"));
                    fs.setElectricityDeposit(rs.getDouble("electricity_deposit"));
                    fs.setWifiDeposit(rs.getDouble("wifi_deposit"));
                    fs.setPlanExtraCharge(rs.getDouble("plan_extra_charge"));
                    fs.setKitchenCharge(rs.getDouble("kitchen_charge"));
                    fs.setEffectiveDate(rs.getDate("effective_date"));
                    return fs;
                }
            }
        } catch (SQLException e) { System.err.println("FeeStructureDAO: " + e.getMessage()); }
        return null;
    }
}
