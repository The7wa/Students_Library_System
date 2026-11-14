package com.library.dao;

import com.library.entity.Notification;
import com.library.util.DBUtil;
import com.library.util.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 站内通知数据访问。
 */
public class NotificationDao {

    public void insert(int userId, String type, String message, String link) {
        String sql = "INSERT INTO notification(user_id, type, message, link) VALUES (?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, message);
            ps.setString(4, link);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("保存通知失败", e);
        }
    }

    /**
     * 通知所有管理员（系统变化通知）。
     */
    public void notifyAdmins(String type, String message, String link) {
        String sql = "INSERT INTO notification(user_id, type, message, link) "
                + "SELECT id, ?, ?, ? FROM users WHERE role = 'ADMIN'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, message);
            ps.setString(3, link);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("通知管理员失败", e);
        }
    }

    public List<Notification> findByUser(int userId, int limit) {
        String sql = "SELECT id, user_id, type, message, link, is_read, created_at "
                + "FROM notification WHERE user_id = ? ORDER BY id DESC LIMIT ?";
        List<Notification> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询通知失败", e);
        }
        return list;
    }

    public int unreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notification WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("统计未读通知失败", e);
        }
    }

    public void markAllRead(int userId) {
        String sql = "UPDATE notification SET is_read = 1 WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("更新通知失败", e);
        }
    }

    /** 清空本人所有通知。 */
    public int deleteAllByUser(int userId) {
        String sql = "DELETE FROM notification WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("清空通知失败", e);
        }
    }

    public void markRead(int id, int userId) {
        String sql = "UPDATE notification SET is_read = 1 WHERE id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("更新通知失败", e);
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setType(rs.getString("type"));
        n.setMessage(rs.getString("message"));
        n.setLink(rs.getString("link"));
        n.setRead(rs.getInt("is_read") == 1);
        Timestamp createdAt = rs.getTimestamp("created_at");
        n.setCreatedAt(createdAt);
        return n;
    }
}
