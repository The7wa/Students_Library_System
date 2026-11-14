package com.library.dao;

import com.library.util.DBUtil;
import com.library.util.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 批注点赞：同一用户对同一批注只能点一次，再次点击取消。
 */
public class AnnotationLikeDao {

    /**
     * @return true 表示当前是已点赞状态，false 表示已取消
     */
    public boolean toggle(int annotationId, int userId) {
        try (Connection conn = DBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM annotation_like WHERE annotation_id=? AND user_id=?")) {
                ps.setInt(1, annotationId);
                ps.setInt(2, userId);
                if (ps.executeUpdate() > 0) {
                    return false;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO annotation_like(annotation_id, user_id) VALUES (?,?)")) {
                ps.setInt(1, annotationId);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            // 并发重复点赞时唯一键冲突，按已点赞处理
            if (e.getErrorCode() == 1062) {
                return true;
            }
            throw new DataAccessException("点赞失败", e);
        }
    }

    public boolean exists(int annotationId, int userId) {
        String sql = "SELECT 1 FROM annotation_like WHERE annotation_id=? AND user_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, annotationId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询点赞失败", e);
        }
    }
}
