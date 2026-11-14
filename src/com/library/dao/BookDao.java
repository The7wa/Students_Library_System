package com.library.dao;

import com.library.entity.Book;
import com.library.entity.Page;
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
 * 图书数据访问。所有用户输入均走 PreparedStatement，排序字段使用白名单。
 */
public class BookDao {

    private static final String SELECT_FIELDS =
            "SELECT b.id, b.category_id, b.name, b.author, b.isbn, b.publisher, "
                    + "b.description, b.content_file, b.content_name, b.uploader_id, b.status, "
                    + "b.create_time, b.update_time, "
                    + "c.name AS category_name, c.icon AS category_icon, c.color AS category_color, "
                    + "u.nickname AS uploader_name, "
                    + "(SELECT ROUND(AVG(r.stars), 1) FROM rating r WHERE r.book_id = b.id) AS avg_stars, "
                    + "(SELECT COUNT(*) FROM rating r WHERE r.book_id = b.id) AS rating_count, "
                    + "(b.id IN (SELECT id FROM (SELECT id FROM book WHERE status = 'APPROVED' "
                    + "ORDER BY create_time DESC, id DESC LIMIT 3) recent)) AS is_new "
                    + "FROM book b JOIN category c ON b.category_id = c.id "
                    + "LEFT JOIN users u ON u.id = b.uploader_id ";

    /**
     * 分页查询书架 / 检索（只显示已上架图书）。categoryId 为 null 时表示全部板块。
     */
    public Page<Book> search(Integer categoryId, String keyword, String sort, int page, int pageSize) {
        String cleanKeyword = keyword == null ? "" : keyword.trim();
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE b.status = 'APPROVED' ");
        if (categoryId != null) {
            where.append(" AND b.category_id = ? ");
            params.add(categoryId);
        }
        if (!cleanKeyword.isEmpty()) {
            where.append(" AND (b.name LIKE ? OR b.author LIKE ? OR b.isbn LIKE ? OR b.publisher LIKE ?) ");
            String like = "%" + cleanKeyword + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        int total = count(where.toString(), params);
        int totalPages = Math.max(1, (total + pageSize - 1) / pageSize);
        int currentPage = Math.min(Math.max(1, page), totalPages);
        int offset = (currentPage - 1) * pageSize;

        String sql = SELECT_FIELDS + where + " ORDER BY " + safeSort(sort) + " LIMIT ? OFFSET ?";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            for (Object param : params) {
                ps.setObject(index++, param);
            }
            ps.setInt(index++, pageSize);
            ps.setInt(index, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询图书失败", e);
        }
        return new Page<>(books, currentPage, pageSize, total);
    }

    public Book findById(int id) {
        String sql = SELECT_FIELDS + " WHERE b.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询图书失败", e);
        }
    }

    public void insert(Book book) {
        String sql = "INSERT INTO book(category_id, name, author, isbn, publisher, description, "
                + "content_file, content_name, uploader_id, status) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, book);
            if (book.getUploaderId() != null) {
                ps.setInt(9, book.getUploaderId());
            } else {
                ps.setNull(9, java.sql.Types.INTEGER);
            }
            ps.setString(10, book.getStatus() == null ? "APPROVED" : book.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    book.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("新增图书失败", e);
        }
    }

    public boolean update(Book book) {
        String sql = "UPDATE book SET category_id=?, name=?, author=?, isbn=?, publisher=?, "
                + "description=?, content_file=?, content_name=?, status=?, uploader_id=?, "
                + "update_time=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, book);
            ps.setString(9, book.getStatus() == null ? "APPROVED" : book.getStatus());
            if (book.getUploaderId() != null) {
                ps.setInt(10, book.getUploaderId());
            } else {
                ps.setNull(10, java.sql.Types.INTEGER);
            }
            ps.setInt(11, book.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("修改图书失败", e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM book WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("删除图书失败", e);
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM book WHERE status = 'APPROVED'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("统计图书失败", e);
        }
    }

    /**
     * 待审核图书数量，供管理员提示。
     */
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM book WHERE status = 'PENDING'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("统计待审核图书失败", e);
        }
    }

    /**
     * 待审核图书列表（管理员）。
     */
    public List<Book> findPending() {
        String sql = SELECT_FIELDS + " WHERE b.status = 'PENDING' ORDER BY b.create_time ASC, b.id ASC";
        return list(sql);
    }

    /**
     * 某个用户上传的全部图书（含各状态），用于"我的上传"。
     */
    public List<Book> findByUploader(int uploaderId) {
        String sql = SELECT_FIELDS + " WHERE b.uploader_id = ? ORDER BY b.create_time DESC, b.id DESC";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, uploaderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询上传图书失败", e);
        }
        return books;
    }

    /**
     * 最近上新的图书（默认 3 本），用于首页推荐。
     */
    public List<Book> findLatest(int limit) {
        String sql = SELECT_FIELDS + " WHERE b.status = 'APPROVED' "
                + "ORDER BY b.create_time DESC, b.id DESC LIMIT ?";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询最新图书失败", e);
        }
        return books;
    }

    /**
     * 更新审核状态。
     */
    public boolean setStatus(int id, String status) {
        String sql = "UPDATE book SET status = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("更新审核状态失败", e);
        }
    }

    private List<Book> list(String sql) {
        List<Book> books = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                books.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询图书失败", e);
        }
        return books;
    }

    /**
     * 已收录正文、可以在线阅读的图书数量。
     */
    public int countReadable() {
        String sql = "SELECT COUNT(*) FROM book WHERE status = 'APPROVED' "
                + "AND content_file IS NOT NULL AND content_file <> ''";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("统计可阅读图书失败", e);
        }
    }

    private int count(String where, List<Object> params) {
        String sql = "SELECT COUNT(*) FROM book b JOIN category c ON b.category_id = c.id " + where;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("统计图书失败", e);
        }
    }

    private String safeSort(String sort) {
        if (sort == null) {
            return "b.create_time DESC, b.id DESC";
        }
        switch (sort) {
            case "nameAsc":
                return "b.name ASC";
            case "oldest":
                return "b.create_time ASC, b.id ASC";
            default:
                return "b.create_time DESC, b.id DESC";
        }
    }

    private void bind(PreparedStatement ps, Book book) throws SQLException {
        ps.setInt(1, book.getCategoryId());
        ps.setString(2, book.getName());
        ps.setString(3, book.getAuthor());
        ps.setString(4, book.getIsbn());
        ps.setString(5, book.getPublisher());
        ps.setString(6, book.getDescription());
        ps.setString(7, book.getContentFile());
        ps.setString(8, book.getContentName());
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setCategoryId(rs.getInt("category_id"));
        book.setCategoryName(rs.getString("category_name"));
        book.setCategoryIcon(rs.getString("category_icon"));
        book.setCategoryColor(rs.getString("category_color"));
        book.setName(rs.getString("name"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setPublisher(rs.getString("publisher"));
        book.setDescription(rs.getString("description"));
        book.setContentFile(rs.getString("content_file"));
        book.setContentName(rs.getString("content_name"));
        int uploaderId = rs.getInt("uploader_id");
        book.setUploaderId(rs.wasNull() ? null : uploaderId);
        book.setUploaderName(rs.getString("uploader_name"));
        book.setStatus(rs.getString("status"));
        book.setAvgStars(rs.getDouble("avg_stars"));
        book.setRatingCount(rs.getInt("rating_count"));
        book.setNewArrival(rs.getInt("is_new") == 1);
        book.setCreateTime(rs.getTimestamp("create_time"));
        book.setUpdateTime(rs.getTimestamp("update_time"));
        return book;
    }
}
