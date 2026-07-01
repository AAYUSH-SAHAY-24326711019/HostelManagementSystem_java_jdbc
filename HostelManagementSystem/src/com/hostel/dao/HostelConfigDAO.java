package com.hostel.dao;

import com.hostel.util.DBConnection;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class HostelConfigDAO {

    public Map<String, String> getConfig() {
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT * FROM hostel_config LIMIT 1";
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                map.put("hostel_name", rs.getString("hostel_name"));
                map.put("address", rs.getString("address"));
                map.put("contact_number", rs.getString("contact_number"));
                map.put("email", rs.getString("email"));
                map.put("established_year", String.valueOf(rs.getInt("established_year")));
            }
        } catch (SQLException e) { System.err.println("HostelConfigDAO.get: " + e.getMessage()); }
        return map;
    }

    public boolean updateConfig(String name, String address, String contact, String email, int year) {
        String sql = "UPDATE hostel_config SET hostel_name=?, address=?, contact_number=?, email=?, established_year=? WHERE config_id=1";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, address); ps.setString(3, contact);
            ps.setString(4, email); ps.setInt(5, year);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("HostelConfigDAO.update: " + e.getMessage()); return false; }
    }

    public String getHostelName() {
        Map<String, String> cfg = getConfig();
        return cfg.getOrDefault("hostel_name", "Girls Hostel");
    }
}
