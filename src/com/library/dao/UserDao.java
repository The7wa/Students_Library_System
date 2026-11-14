package com.library.dao;

import com.library.entity.User;
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
 * 用户数据访问。
 */
public class UserDao {

    public User findByUsername(String username) {
        String sql = "SELECT id, username, password, nickname, email, role, created_at "
                + "FROM users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询用户失败", e);
        }
    }

    public User findById(int id) {
        String sql = "SELECT id, username, password, nickname, email, role, created_at "
                + "FROM users WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询用户失败", e);
        }
    }

    /**
     * 全部用户列表（管理员页）。
     */
    public List<User> findAll() {
        String sql = "SELECT id, username, password, nickname, email, role, created_at "
                + "FROM users ORDER BY id ASC";
        List<User> users = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询用户列表失败", e);
        }
        return users;
    }

    /**
     * 管理员数量，防止把最后一个管理员降级或删除。
     */
    public int countAdmins() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("统计管理员失败", e);
        }
    }

    /**
     * 更新用户资料（密码为 null 表示不改）。
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET nickname=?, email=?, role=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNickname());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setInt(4, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("修改用户失败", e);
        }
    }

    public boolean updatePassword(int id, String passwordHash) {
        String sql = "UPDATE users SET password=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("修改密码失败", e);
        }
    }

    public boolean delete(int id) {
        // 清理该用户的评论、批注、评分、通知，并保留其上传图书（上传者置空）
        String[] cleanupSql = {
                "DELETE FROM comment WHERE user_id = ?",
                "DELETE FROM annotation WHERE user_id = ?",
                "DELETE FROM rating WHERE user_id = ?",
                "DELETE FROM notification WHERE user_id = ?",
                "UPDATE book SET uploader_id = NULL WHERE uploader_id = ?"
        };
        try (Connection conn = DBUtil.getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                for (String sql : cleanupSql) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                }
                int rows;
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
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
            throw new DataAccessException("删除用户失败", e);
        }
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("检查用户名失败", e);
        }
    }

    public void insert(User user) {
        String sql = "INSERT INTO users(username, password, nickname, email, role) VALUES (?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getNickname());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("注册用户失败", e);
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("统计用户失败", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setNickname(rs.getString("nickname"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        user.setCreatedAt(createdAt);
        return user;
    }
}
