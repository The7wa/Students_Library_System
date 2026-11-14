package com.library.dao;

import com.library.entity.Comment;
import com.library.util.DBUtil;
import com.library.util.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 章节评论数据访问（所有人可见）。
 */
public class CommentDao {

    /**
     * 某本书某一章的评论，按时间正序。
     */
    public List<Comment> findByBookAndChapter(int bookId, int chapterIndex) {
        String sql = "SELECT c.id, c.book_id, c.chapter_index, c.user_id, c.content, c.created_at, "
                + "u.username, u.nickname "
                + "FROM comment c JOIN users u ON u.id = c.user_id "
                + "WHERE c.book_id = ? AND c.chapter_index = ? "
                + "ORDER BY c.id ASC";
        List<Comment> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, chapterIndex);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询评论失败", e);
        }
        return list;
    }

    public void insert(Comment c) {
        String sql = "INSERT INTO comment(book_id, chapter_index, user_id, content) VALUES (?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getBookId());
            ps.setInt(2, c.getChapterIndex());
            ps.setInt(3, c.getUserId());
            ps.setString(4, c.getContent());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("保存评论失败", e);
        }
    }

    public Comment findById(int id) {
        String sql = "SELECT c.id, c.book_id, c.chapter_index, c.user_id, c.content, c.created_at, "
                + "u.username, u.nickname "
                + "FROM comment c JOIN users u ON u.id = c.user_id WHERE c.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询评论失败", e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM comment WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("删除评论失败", e);
        }
    }

    private Comment mapRow(ResultSet rs) throws SQLException {
        Comment c = new Comment();
        c.setId(rs.getInt("id"));
        c.setBookId(rs.getInt("book_id"));
        c.setChapterIndex(rs.getInt("chapter_index"));
        c.setUserId(rs.getInt("user_id"));
        c.setUsername(rs.getString("username"));
        c.setNickname(rs.getString("nickname"));
        c.setContent(rs.getString("content"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        return c;
    }
}
