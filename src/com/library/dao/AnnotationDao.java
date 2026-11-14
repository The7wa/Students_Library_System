package com.library.dao;

import com.library.entity.Annotation;
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
 * 阅读批注数据访问（仅本人可见，查询和删除都带 user_id 条件）。
 */
public class AnnotationDao {

    /**
     * 某用户在某本书上的全部批注。
     */
    public List<Annotation> findByBookAndUser(int bookId, int userId) {
        String sql = "SELECT id, book_id, user_id, chapter_index, paragraph_index, quote, note, color, created_at "
                + "FROM annotation WHERE book_id = ? AND user_id = ? ORDER BY chapter_index ASC, paragraph_index ASC, id ASC";
        List<Annotation> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询批注失败", e);
        }
        return list;
    }

    public void insert(Annotation a) {
        String sql = "INSERT INTO annotation(book_id, user_id, chapter_index, paragraph_index, quote, note, color) "
                + "VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getBookId());
            ps.setInt(2, a.getUserId());
            ps.setInt(3, a.getChapterIndex());
            ps.setInt(4, a.getParagraphIndex());
            ps.setString(5, a.getQuote());
            ps.setString(6, a.getNote());
            ps.setString(7, a.getColor());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("保存批注失败", e);
        }
    }

    /**
     * 删除自己的批注。
     */
    public boolean delete(int id, int userId) {
        String sql = "DELETE FROM annotation WHERE id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("删除批注失败", e);
        }
    }

    private Annotation mapRow(ResultSet rs) throws SQLException {
        Annotation a = new Annotation();
        a.setId(rs.getInt("id"));
        a.setBookId(rs.getInt("book_id"));
        a.setUserId(rs.getInt("user_id"));
        a.setChapterIndex(rs.getInt("chapter_index"));
        a.setParagraphIndex(rs.getInt("paragraph_index"));
        a.setQuote(rs.getString("quote"));
        a.setNote(rs.getString("note"));
        a.setColor(rs.getString("color"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}
