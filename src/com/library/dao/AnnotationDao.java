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
 * 阅读批注数据访问。
 *
 * <p>同一用户、同一本书、同一章节、同一段落、同一段原文只保留最新一条：
 * 再次标记时更新颜色 / 笔记，而不是追加日志。</p>
 */
public class AnnotationDao {

    private static final String BASE_SELECT =
            "SELECT a.id, a.book_id, a.user_id, a.chapter_index, a.paragraph_index, a.quote, "
                    + "a.note, a.color, a.created_at, "
                    + "u.username, u.nickname, u.avatar, "
                    + "b.name AS book_name, b.author AS book_author, c.color AS book_color, "
                    + "(b.content_file IS NOT NULL AND b.content_file <> '') AS book_has_content ";

    private static final String BASE_FROM =
            "FROM annotation a "
                    + "JOIN users u ON u.id = a.user_id "
                    + "JOIN book b ON b.id = a.book_id "
                    + "JOIN category c ON c.id = b.category_id ";

    private static final String LATEST_PER_QUOTE =
            " AND a.id = (SELECT MAX(a2.id) FROM annotation a2 "
                    + "WHERE a2.user_id = a.user_id AND a2.book_id = a.book_id "
                    + "AND a2.chapter_index = a.chapter_index AND a2.paragraph_index = a.paragraph_index "
                    + "AND a2.quote = a.quote) ";

    /** 某用户在某本书上的批注（每段原文只取最新一条）。 */
    public List<Annotation> findByBookAndUser(int bookId, int userId) {
        String sql = BASE_SELECT + BASE_FROM
                + "WHERE a.book_id = ? AND a.user_id = ? " + LATEST_PER_QUOTE
                + "ORDER BY a.chapter_index ASC, a.paragraph_index ASC, a.id ASC";
        List<Annotation> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Annotation a = mapRow(rs, false);
                    a.setMine(true);
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询批注失败", e);
        }
        return list;
    }

    /**
     * 某本书某章所有读者的公开批注（原文与颜色公开，笔记仍只在 JSP 中对本人展示）。
     */
    public List<Annotation> findByBookAndChapter(int bookId, int chapterIndex, int viewerId) {
        String sql = BASE_SELECT
                + ", (SELECT COUNT(*) FROM annotation_like l WHERE l.annotation_id = a.id) AS like_count "
                + ", (SELECT COUNT(*) FROM annotation_comment ac WHERE ac.annotation_id = a.id) AS comment_count "
                + ", EXISTS(SELECT 1 FROM annotation_like l2 WHERE l2.annotation_id = a.id AND l2.user_id = ?) AS liked_by_me "
                + BASE_FROM
                + "WHERE a.book_id = ? AND a.chapter_index = ? " + LATEST_PER_QUOTE
                + "ORDER BY a.created_at DESC, a.id DESC";
        List<Annotation> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, viewerId);
            ps.setInt(2, bookId);
            ps.setInt(3, chapterIndex);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Annotation a = mapRow(rs, true);
                    a.setMine(a.getUserId() == viewerId);
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询书友批注失败", e);
        }
        return list;
    }

    /** 我的笔记汇总（所有书，每段原文最新一条）。 */
    public List<Annotation> findLatestByUser(int userId) {
        String sql = BASE_SELECT + BASE_FROM
                + "WHERE a.user_id = ? " + LATEST_PER_QUOTE
                + "ORDER BY a.created_at DESC, a.id DESC";
        List<Annotation> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Annotation a = mapRow(rs, false);
                    a.setMine(true);
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询我的笔记失败", e);
        }
        return list;
    }

    public Annotation findById(int id) {
        String sql = BASE_SELECT + BASE_FROM + "WHERE a.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs, false) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询批注失败", e);
        }
    }

    /**
     * 新增或更新批注：同一段原文再次标记时只更新最后一条。
     * note 为空时保留原来的笔记，只改变颜色。
     */
    public Annotation upsert(Annotation a) {
        Integer existingId = findExistingId(a);
        try (Connection conn = DBUtil.getConnection()) {
            if (existingId != null) {
                String sql = "UPDATE annotation SET quote=?, color=?, "
                        + "note = CASE WHEN ? = '' THEN note ELSE ? END, "
                        + "created_at = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, a.getQuote());
                    ps.setString(2, a.getColor());
                    ps.setString(3, a.getNote());
                    ps.setString(4, a.getNote());
                    ps.setInt(5, existingId);
                    ps.executeUpdate();
                }
                a.setId(existingId);
            } else {
                String sql = "INSERT INTO annotation(book_id, user_id, chapter_index, paragraph_index, quote, note, color) "
                        + "VALUES (?,?,?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("保存批注失败", e);
        }
        return a;
    }

    private Integer findExistingId(Annotation a) {
        String sql = "SELECT id FROM annotation "
                + "WHERE user_id=? AND book_id=? AND chapter_index=? AND paragraph_index=? AND quote=? "
                + "ORDER BY id DESC LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getUserId());
            ps.setInt(2, a.getBookId());
            ps.setInt(3, a.getChapterIndex());
            ps.setInt(4, a.getParagraphIndex());
            ps.setString(5, a.getQuote());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询批注失败", e);
        }
    }

    /** 删除自己的批注，同时清掉它下面的评论和点赞。 */
    public boolean delete(int id, int userId) {
        try (Connection conn = DBUtil.getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM annotation_comment WHERE annotation_id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM annotation_like WHERE annotation_id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                int rows;
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM annotation WHERE id = ? AND user_id = ?")) {
                    ps.setInt(1, id);
                    ps.setInt(2, userId);
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
            throw new DataAccessException("删除批注失败", e);
        }
    }

    public int countByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM ("
                + "SELECT book_id, chapter_index, paragraph_index, quote FROM annotation "
                + "WHERE user_id = ? GROUP BY book_id, chapter_index, paragraph_index, quote) t";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("统计批注失败", e);
        }
    }

    private Annotation mapRow(ResultSet rs, boolean withSocial) throws SQLException {
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

        String nickname = rs.getString("nickname");
        a.setUserName(nickname == null || nickname.isBlank() ? rs.getString("username") : nickname);
        a.setUserAvatar(rs.getString("avatar"));
        a.setBookName(rs.getString("book_name"));
        a.setBookAuthor(rs.getString("book_author"));
        a.setBookColor(rs.getString("book_color"));
        a.setBookHasContent(rs.getBoolean("book_has_content"));

        if (withSocial) {
            a.setLikeCount(rs.getInt("like_count"));
            a.setCommentCount(rs.getInt("comment_count"));
            a.setLikedByMe(rs.getBoolean("liked_by_me"));
        }
        return a;
    }
}
