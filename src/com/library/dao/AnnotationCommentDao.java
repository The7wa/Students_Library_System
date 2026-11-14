package com.library.dao;

import com.library.entity.AnnotationComment;
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
 * 批注评论 / 回复。
 */
public class AnnotationCommentDao {

    public List<AnnotationComment> findByBookAndChapter(int bookId, int chapterIndex, int viewerId) {
        String sql = "SELECT ac.id, ac.annotation_id, ac.user_id, ac.parent_id, ac.content, ac.created_at, "
                + "u.username, u.nickname, u.avatar "
                + "FROM annotation_comment ac "
                + "JOIN users u ON u.id = ac.user_id "
                + "JOIN annotation a ON a.id = ac.annotation_id "
                + "WHERE a.book_id = ? AND a.chapter_index = ? "
                + "ORDER BY ac.created_at ASC, ac.id ASC";
        List<AnnotationComment> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, chapterIndex);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs, viewerId));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询批注评论失败", e);
        }
        return list;
    }

    public void insert(AnnotationComment c) {
        String sql = "INSERT INTO annotation_comment(annotation_id, user_id, parent_id, content) VALUES (?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getAnnotationId());
            ps.setInt(2, c.getUserId());
            if (c.getParentId() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, c.getParentId());
            }
            ps.setString(4, c.getContent());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("保存批注评论失败", e);
        }
    }

    public AnnotationComment findById(int id) {
        String sql = "SELECT ac.id, ac.annotation_id, ac.user_id, ac.parent_id, ac.content, ac.created_at, "
                + "u.username, u.nickname, u.avatar "
                + "FROM annotation_comment ac JOIN users u ON u.id = ac.user_id WHERE ac.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs, 0) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询批注评论失败", e);
        }
    }

    public boolean delete(int id, int userId, boolean admin) {
        AnnotationComment c = findById(id);
        if (c == null || (!admin && c.getUserId() != userId)) {
            return false;
        }
        try (Connection conn = DBUtil.getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM annotation_comment WHERE parent_id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                int rows;
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM annotation_comment WHERE id = ?")) {
                    ps.setInt(1, id);
                    rows = ps.executeUpdate();
                }
                conn.commit();
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }
        } catch (SQLException e) {
            throw new DataAccessException("删除批注评论失败", e);
        }
    }

    private AnnotationComment mapRow(ResultSet rs, int viewerId) throws SQLException {
        AnnotationComment c = new AnnotationComment();
        c.setId(rs.getInt("id"));
        c.setAnnotationId(rs.getInt("annotation_id"));
        c.setUserId(rs.getInt("user_id"));
        int parent = rs.getInt("parent_id");
        c.setParentId(rs.wasNull() ? null : parent);
        c.setContent(rs.getString("content"));
        Timestamp t = rs.getTimestamp("created_at");
        c.setCreatedAt(t);
        String nickname = rs.getString("nickname");
        c.setUserName(nickname == null || nickname.isBlank() ? rs.getString("username") : nickname);
        c.setUserAvatar(rs.getString("avatar"));
        c.setMine(viewerId > 0 && c.getUserId() == viewerId);
        return c;
    }
}
