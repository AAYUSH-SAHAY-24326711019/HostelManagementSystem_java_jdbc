package com.hostel.dao;

import com.hostel.model.Notice;
import com.hostel.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {

    public boolean addNotice(Notice n, List<Integer> girlIds, List<Integer> parentIds) {
        String sql = "INSERT INTO notices (title,message,target_type,target_girl_id,created_by) VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, n.getTitle()); ps.setString(2, n.getMessage());
            ps.setString(3, n.getTargetType());
            if (n.getTargetGirlId() != null) ps.setInt(4, n.getTargetGirlId()); else ps.setNull(4, Types.INTEGER);
            ps.setInt(5, n.getCreatedBy()); ps.executeUpdate();
            int noticeId;
            try (ResultSet rs = ps.getGeneratedKeys()) { if (!rs.next()) return false; noticeId = rs.getInt(1); }
            // Fan-out to recipients
            String recSql = "INSERT INTO notice_recipients (notice_id, girl_id, parent_id) VALUES (?,?,?)";
            try (PreparedStatement rps = con.prepareStatement(recSql)) {
                for (Integer gid : girlIds) {
                    rps.setInt(1, noticeId); rps.setInt(2, gid); rps.setNull(3, Types.INTEGER); rps.addBatch();
                }
                for (Integer pid : parentIds) {
                    rps.setInt(1, noticeId); rps.setNull(2, Types.INTEGER); rps.setInt(3, pid); rps.addBatch();
                }
                rps.executeBatch();
            }
            return true;
        } catch (SQLException e) { System.err.println("NoticeDAO.add: " + e.getMessage()); return false; }
    }

    public List<Notice> getNoticesForGirl(int girlId) {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT n.* FROM notices n JOIN notice_recipients nr ON n.notice_id=nr.notice_id " +
                     "WHERE nr.girl_id=? ORDER BY n.created_date DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, girlId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapNotice(rs)); }
        } catch (SQLException e) { System.err.println("NoticeDAO.forGirl: " + e.getMessage()); }
        return list;
    }

    public List<Notice> getNoticesForParent(int parentId) {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT n.* FROM notices n JOIN notice_recipients nr ON n.notice_id=nr.notice_id " +
                     "WHERE nr.parent_id=? ORDER BY n.created_date DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapNotice(rs)); }
        } catch (SQLException e) { System.err.println("NoticeDAO.forParent: " + e.getMessage()); }
        return list;
    }

    public List<Notice> getAllNotices() {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT n.*, g.name AS target_girl_name FROM notices n " +
                     "LEFT JOIN girls g ON n.target_girl_id=g.girl_id ORDER BY n.created_date DESC";
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Notice notice = mapNotice(rs);
                try { notice.setTargetGirlName(rs.getString("target_girl_name")); } catch (Exception ignored) {}
                list.add(notice);
            }
        } catch (SQLException e) { System.err.println("NoticeDAO.getAll: " + e.getMessage()); }
        return list;
    }

    private Notice mapNotice(ResultSet rs) throws SQLException {
        Notice n = new Notice();
        n.setNoticeId(rs.getInt("notice_id")); n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message")); n.setTargetType(rs.getString("target_type"));
        n.setCreatedBy(rs.getInt("created_by")); n.setCreatedDate(rs.getTimestamp("created_date"));
        try { Object gid = rs.getObject("target_girl_id"); if (gid != null) n.setTargetGirlId((Integer) gid); } catch (Exception ignored) {}
        return n;
    }
}
