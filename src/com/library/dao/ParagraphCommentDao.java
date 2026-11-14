package com.library.dao;

import com.library.entity.ParagraphComment;
import com.library.util.DBUtil;
import com.library.util.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 段落公开评论数据访问。
 */
public class ParagraphCommentDao {

    public Map<Integer, Integer> countsByChapter(int bookId, int chapterIndex) {
        String sql = "SELECT paragraph_index, COUNT(*) AS c FROM paragraph_comment "
                + "WHERE book_id=? AND chapter_index=? GROUP BY paragraph_index";
        Map<Integer, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, chapterIndex);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("paragraph_index"), rs.getInt("c"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("统计段落评论失败", e);
        }
        return map;
    }

    public List<ParagraphComment> findByChapter(int bookId, int chapterIndex, int viewerId) {
        String sql = "SELECT pc.id, pc.book_id, pc.chapter_index, pc.paragraph_index, pc.quote, "
                + "pc.user_id, pc.parent_id, pc.content, pc.created_at, "
                + "u.username, u.nickname, u.avatar "
                + "FROM paragraph_comment pc "
                + "JOIN users u ON u.id = pc.user_id "
                + "WHERE pc.book_id=? AND pc.chapter_index=? "
                + "ORDER BY pc.created_at ASC, pc.id ASC";
        List<ParagraphComment> list = new ArrayList<>();
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
            throw new DataAccessException("查询段落评论失败", e);
        }
        return list;
    }

    public ParagraphComment insert(ParagraphComment c) {
        String sql = "INSERT INTO paragraph_comment(book_id, chapter_index, paragraph_index, quote, "
                + "user_id, parent_id, content) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getBookId());
            ps.setInt(2, c.getChapterIndex());
            ps.setInt(3, c.getParagraphIndex());
            ps.setString(4, c.getQuote());
            ps.setInt(5, c.getUserId());
            if (c.getParentId() == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, c.getParentId());
            }
            ps.setString(7, c.getContent());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("保存段落评论失败", e);
        }
        return c;
    }

    public ParagraphComment findById(int id, int viewerId) {
        String sql = "SELECT pc.id, pc.book_id, pc.chapter_index, pc.paragraph_index, pc.quote, "
                + "pc.user_id, pc.parent_id, pc.content, pc.created_at, "
                + "u.username, u.nickname, u.avatar "
                + "FROM paragraph_comment pc JOIN users u ON u.id = pc.user_id WHERE pc.id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs, viewerId) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询段落评论失败", e);
        }
    }

    private ParagraphComment mapRow(ResultSet rs, int viewerId) throws SQLException {
        ParagraphComment c = new ParagraphComment();
        c.setId(rs.getInt("id"));
        c.setBookId(rs.getInt("book_id"));
        c.setChapterIndex(rs.getInt("chapter_index"));
        c.setParagraphIndex(rs.getInt("paragraph_index"));
        c.setQuote(rs.getString("quote"));
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
