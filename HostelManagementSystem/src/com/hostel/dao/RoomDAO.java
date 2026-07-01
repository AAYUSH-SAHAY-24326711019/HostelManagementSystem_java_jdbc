package com.hostel.dao;

import com.hostel.model.*;
import com.hostel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public List<Room> getAll() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.*, rp.plan_name FROM rooms r LEFT JOIN room_plans rp ON r.plan_id=rp.plan_id WHERE r.is_active=1";
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRoom(rs));
        } catch (SQLException e) { System.err.println("RoomDAO.getAll: " + e.getMessage()); }
        return list;
    }

    public List<Room> getAvailableRooms(int planId) {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.*, rp.plan_name FROM rooms r LEFT JOIN room_plans rp ON r.plan_id=rp.plan_id " +
                     "WHERE r.is_active=1 AND r.plan_id=? AND r.occupied_count < r.capacity";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapRoom(rs)); }
        } catch (SQLException e) { System.err.println("RoomDAO.getAvailable: " + e.getMessage()); }
        return list;
    }

    public boolean incrementOccupancy(int roomId) {
        return updateCount(roomId, "+1");
    }

    public boolean decrementOccupancy(int roomId) {
        return updateCount(roomId, "-1");
    }

    private boolean updateCount(int roomId, String op) {
        String sql = "UPDATE rooms SET occupied_count = occupied_count " + op + " WHERE room_id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomId); return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    private Room mapRoom(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId(rs.getInt("room_id")); r.setRoomNumber(rs.getString("room_number"));
        r.setPlanId(rs.getInt("plan_id")); r.setCapacity(rs.getInt("capacity"));
        r.setOccupiedCount(rs.getInt("occupied_count")); r.setFloorNumber(rs.getInt("floor_number"));
        r.setActive(rs.getInt("is_active") == 1);
        try { r.setPlanName(rs.getString("plan_name")); } catch (SQLException ignored) {}
        return r;
    }

    // ---- RoomPlan helpers -----
    public List<RoomPlan> getAllPlans() {
        List<RoomPlan> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM room_plans WHERE is_active=1")) {
            while (rs.next()) {
                RoomPlan p = new RoomPlan();
                p.setPlanId(rs.getInt("plan_id")); p.setPlanName(rs.getString("plan_name"));
                p.setDescription(rs.getString("description"));
                p.setBaseCharge(rs.getDouble("base_charge")); p.setExtraCharge(rs.getDouble("extra_charge"));
                p.setActive(rs.getInt("is_active") == 1);
                list.add(p);
            }
        } catch (SQLException e) { System.err.println("RoomDAO.getAllPlans: " + e.getMessage()); }
        return list;
    }

    // ---- KitchenPlan helpers -----
    public List<KitchenPlan> getAllKitchenPlans() {
        List<KitchenPlan> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM kitchen_plans WHERE is_active=1")) {
            while (rs.next()) {
                KitchenPlan k = new KitchenPlan();
                k.setKitchenPlanId(rs.getInt("kitchen_plan_id")); k.setPlanName(rs.getString("plan_name"));
                k.setMonthlyCharge(rs.getDouble("monthly_charge")); k.setRefundable(rs.getInt("is_refundable") == 1);
                k.setActive(rs.getInt("is_active") == 1);
                list.add(k);
            }
        } catch (SQLException e) { System.err.println("RoomDAO.getAllKitchenPlans: " + e.getMessage()); }
        return list;
    }
}
