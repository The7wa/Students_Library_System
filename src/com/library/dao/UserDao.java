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

    private static final String USER_COLUMNS =
            "id, username, password, nickname, email, avatar, bio, lang, role, created_at";

    public User findByUsername(String username) {
        String sql = "SELECT " + USER_COLUMNS + " FROM users WHERE username = ?";
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
        String sql = "SELECT " + USER_COLUMNS + " FROM users WHERE id = ?";
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

    public List<User> findAll() {
        String sql = "SELECT " + USER_COLUMNS + " FROM users ORDER BY id ASC";
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

    /** 管理员后台修改昵称 / 邮箱 / 角色。 */
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

    /** 个人主页修改资料：昵称、邮箱、简介、头像。 */
    public boolean updateProfile(User user) {
        String sql = "UPDATE users SET nickname=?, email=?, bio=?, avatar=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getNickname());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getBio());
            ps.setString(4, user.getAvatar());
            ps.setInt(5, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("修改个人资料失败", e);
        }
    }

    public boolean updateLanguage(int id, String lang) {
        String sql = "UPDATE users SET lang=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lang);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("修改语言设置失败", e);
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

    /**
     * 注销用户：清理其批注、批注评论、点赞、章节评论、评分、通知；
     * 其上传的图书保留，但 uploader_id 置空。
     */
    public boolean delete(int id) {
        String[] cleanupSql = {
                "DELETE FROM annotation_comment WHERE user_id = ?",
                "DELETE FROM annotation_like WHERE user_id = ?",
                "DELETE FROM annotation_comment WHERE annotation_id IN (SELECT id FROM annotation WHERE user_id = ?)",
                "DELETE FROM annotation_like WHERE annotation_id IN (SELECT id FROM annotation WHERE user_id = ?)",
                "DELETE FROM annotation WHERE user_id = ?",
                "DELETE FROM paragraph_comment WHERE user_id = ?",
                "DELETE FROM bookshelf WHERE user_id = ?",
                "DELETE FROM comment WHERE user_id = ?",
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
        String sql = "INSERT INTO users(username, password, nickname, email, avatar, bio, lang, role) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getNickname());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getAvatar());
            ps.setString(6, user.getBio());
            ps.setString(7, user.getLang() == null ? "zh" : user.getLang());
            ps.setString(8, user.getRole());
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

    public int countUploads(int userId) {
        return count("SELECT COUNT(*) FROM book WHERE uploader_id = ?", userId);
    }

    public int countAnnotations(int userId) {
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

    public int countComments(int userId) {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM comment WHERE user_id = ?) + "
                + "(SELECT COUNT(*) FROM annotation_comment WHERE user_id = ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("统计评论失败", e);
        }
    }

    public int countRatings(int userId) {
        return count("SELECT COUNT(*) FROM rating WHERE user_id = ?", userId);
    }

    private int count(String sql, int userId) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("统计失败", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setNickname(rs.getString("nickname"));
        user.setEmail(rs.getString("email"));
        user.setAvatar(rs.getString("avatar"));
        user.setBio(rs.getString("bio"));
        user.setLang(rs.getString("lang"));
        user.setRole(rs.getString("role"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        user.setCreatedAt(createdAt);
        return user;
    }
}
