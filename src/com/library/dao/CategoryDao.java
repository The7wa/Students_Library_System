package com.library.dao;

import com.library.entity.Category;
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
 * 板块（分类）数据访问。
 */
public class CategoryDao {

    /**
     * 所有板块，附带每个板块下的图书数量。
     */
    public List<Category> findAllWithCount() {
        String sql = "SELECT c.id, c.name, c.icon, c.description, c.color, c.sort_order, c.created_at, "
                + "COUNT(b.id) AS book_count "
                + "FROM category c LEFT JOIN book b ON b.category_id = c.id AND b.status = 'APPROVED' "
                + "GROUP BY c.id, c.name, c.icon, c.description, c.color, c.sort_order, c.created_at "
                + "ORDER BY c.sort_order ASC, c.id ASC";
        List<Category> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs, true));
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询板块失败", e);
        }
        return list;
    }

    public Category findById(int id) {
        String sql = "SELECT c.id, c.name, c.icon, c.description, c.color, c.sort_order, c.created_at, "
                + "(SELECT COUNT(*) FROM book b WHERE b.category_id = c.id AND b.status = 'APPROVED') AS book_count "
                + "FROM category c WHERE c.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs, true) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询板块失败", e);
        }
    }

    public boolean existsByName(String name, int excludeId) {
        String sql = "SELECT 1 FROM category WHERE name = ? AND id <> ? LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("检查板块名称失败", e);
        }
    }

    public void insert(Category category) {
        String sql = "INSERT INTO category(name, icon, description, color, sort_order) VALUES (?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, category);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("新增板块失败", e);
        }
    }

    public boolean update(Category category) {
        String sql = "UPDATE category SET name=?, icon=?, description=?, color=?, sort_order=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, category);
            ps.setInt(6, category.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("修改板块失败", e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM category WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("删除板块失败", e);
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM category";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("统计板块失败", e);
        }
    }

    private void bind(PreparedStatement ps, Category category) throws SQLException {
        ps.setString(1, category.getName());
        ps.setString(2, category.getIcon());
        ps.setString(3, category.getDescription());
        ps.setString(4, category.getColor());
        ps.setInt(5, category.getSortOrder());
    }

    private Category mapRow(ResultSet rs, boolean withCount) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setIcon(rs.getString("icon"));
        category.setDescription(rs.getString("description"));
        category.setColor(rs.getString("color"));
        category.setSortOrder(rs.getInt("sort_order"));
        category.setCreatedAt(rs.getTimestamp("created_at"));
        if (withCount) {
            category.setBookCount(rs.getInt("book_count"));
        }
        return category;
    }
}
