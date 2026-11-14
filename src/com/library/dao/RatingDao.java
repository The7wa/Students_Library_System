package com.library.dao;

import com.library.util.DBUtil;
import com.library.util.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 图书评分数据访问。每人每书一票，重复打分覆盖旧分。
 */
public class RatingDao {

    /**
     * 打分（1-5 星），已打过分则更新。
     */
    public void rate(int bookId, int userId, int stars) {
        String sql = "INSERT INTO rating(book_id, user_id, stars) VALUES (?,?,?) "
                + "ON DUPLICATE KEY UPDATE stars = VALUES(stars)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            ps.setInt(3, stars);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("保存评分失败", e);
        }
    }

    /**
     * 某用户对某书的评分，未打分返回 0。
     */
    public int findByUser(int bookId, int userId) {
        String sql = "SELECT stars FROM rating WHERE book_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询评分失败", e);
        }
    }

    /**
     * 平均分（保留 1 位小数），无评分返回 0。
     */
    public double average(int bookId) {
        String sql = "SELECT ROUND(AVG(stars), 1) FROM rating WHERE book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble(1);
                    return rs.wasNull() ? 0 : avg;
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("统计评分失败", e);
        }
    }

    /**
     * 评分人数。
     */
    public int countByBook(int bookId) {
        String sql = "SELECT COUNT(*) FROM rating WHERE book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("统计评分失败", e);
        }
    }
}
