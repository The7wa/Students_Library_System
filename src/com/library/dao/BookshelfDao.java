package com.library.dao;

import com.library.entity.Book;
import com.library.util.DBUtil;
import com.library.util.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户书架数据访问。
 */
public class BookshelfDao {

    public boolean exists(int userId, int bookId) {
        String sql = "SELECT 1 FROM bookshelf WHERE user_id=? AND book_id=? LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询书架失败", e);
        }
    }

    /** 加入书架；重复加入不会产生第二条。 */
    public boolean add(int userId, int bookId) {
        String sql = "INSERT INTO bookshelf(user_id, book_id) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE book_id = book_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("加入书架失败", e);
        }
    }

    public boolean remove(int userId, int bookId) {
        String sql = "DELETE FROM bookshelf WHERE user_id=? AND book_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("移出书架失败", e);
        }
    }

    public int countByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM bookshelf WHERE user_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("统计书架失败", e);
        }
    }

    /** 阅读时更新上次读到第几章；不在书架中则不影响任何行。 */
    public void updateLastRead(int userId, int bookId, int chapter) {
        String sql = "UPDATE bookshelf SET last_chapter=?, last_read_at=CURRENT_TIMESTAMP "
                + "WHERE user_id=? AND book_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(0, chapter));
            ps.setInt(2, userId);
            ps.setInt(3, bookId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("更新阅读进度失败", e);
        }
    }

    /**
     * 书架列表：书籍信息 + 加入时间 + 上次阅读章节。
     */
    public List<Book> findByUser(int userId) {
        String sql = "SELECT b.id, b.category_id, b.name, b.author, b.isbn, b.publisher, b.description, "
                + "b.content_file, b.content_name, b.uploader_id, b.status, b.create_time, b.update_time, "
                + "c.name AS category_name, c.icon AS category_icon, c.color AS category_color, "
                + "s.created_at AS shelf_added_at, s.last_chapter, "
                + "(SELECT ROUND(AVG(r.stars), 1) FROM rating r WHERE r.book_id=b.id) AS avg_stars, "
                + "(SELECT COUNT(*) FROM rating r WHERE r.book_id=b.id) AS rating_count, "
                + "(b.id IN (SELECT id FROM (SELECT id FROM book WHERE status='APPROVED' "
                + "ORDER BY create_time DESC, id DESC LIMIT 3) recent)) AS is_new "
                + "FROM bookshelf s "
                + "JOIN book b ON b.id = s.book_id "
                + "JOIN category c ON c.id = b.category_id "
                + "WHERE s.user_id = ? "
                + "ORDER BY s.created_at DESC, s.id DESC";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询我的书架失败", e);
        }
        return books;
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setCategoryId(rs.getInt("category_id"));
        b.setCategoryName(rs.getString("category_name"));
        b.setCategoryIcon(rs.getString("category_icon"));
        b.setCategoryColor(rs.getString("category_color"));
        b.setName(rs.getString("name"));
        b.setAuthor(rs.getString("author"));
        b.setIsbn(rs.getString("isbn"));
        b.setPublisher(rs.getString("publisher"));
        b.setDescription(rs.getString("description"));
        b.setContentFile(rs.getString("content_file"));
        b.setContentName(rs.getString("content_name"));
        int uploader = rs.getInt("uploader_id");
        b.setUploaderId(rs.wasNull() ? null : uploader);
        b.setStatus(rs.getString("status"));
        b.setAvgStars(rs.getDouble("avg_stars"));
        b.setRatingCount(rs.getInt("rating_count"));
        b.setNewArrival(rs.getBoolean("is_new"));
        b.setCreateTime(rs.getTimestamp("create_time"));
        b.setUpdateTime(rs.getTimestamp("update_time"));
        b.setShelfAddedAt(rs.getTimestamp("shelf_added_at"));
        b.setLastChapter(rs.getInt("last_chapter"));
        return b;
    }
}
