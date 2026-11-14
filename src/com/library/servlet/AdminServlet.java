package com.library.servlet;

import com.library.dao.BookDao;
import com.library.dao.NotificationDao;
import com.library.dao.UserDao;
import com.library.entity.Book;
import com.library.entity.User;
import com.library.util.PasswordUtil;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 管理员后台：用户管理、图书审核。
 */
public class AdminServlet extends HttpServlet {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,20}$");

    private final UserDao userDao = new UserDao();
    private final BookDao bookDao = new BookDao();
    private final NotificationDao notificationDao = new NotificationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!requireAdmin(req, resp)) {
            return;
        }
        List<User> users = userDao.findAll();
        List<Book> pendingBooks = bookDao.findPending();
        req.setAttribute("users", users);
        req.setAttribute("pendingBooks", pendingBooks);
        req.setAttribute("adminCount", userDao.countAdmins());
        req.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!requireAdmin(req, resp)) {
            return;
        }
        String action = WebUtil.trim(req.getParameter("action"));
        switch (action) {
            case "saveUser":
                saveUser(req, resp);
                break;
            case "deleteUser":
                deleteUser(req, resp);
                break;
            case "approve":
                review(req, resp, true);
                break;
            case "reject":
                review(req, resp, false);
                break;
            default:
                WebUtil.redirect(req, resp, "/admin");
        }
    }

    private void saveUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User current = (User) req.getSession().getAttribute("loginUser");
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        String username = WebUtil.trim(req.getParameter("username"));
        String nickname = WebUtil.trim(req.getParameter("nickname"));
        String email = WebUtil.trim(req.getParameter("email"));
        String role = "ADMIN".equalsIgnoreCase(WebUtil.trim(req.getParameter("role"))) ? "ADMIN" : "USER";
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");

        User existing = id > 0 ? userDao.findById(id) : null;

        // admin 是最高管理员：只有 admin 本人能修改该账号，且不能降级
        if (id == 0 && "admin".equalsIgnoreCase(username)) {
            WebUtil.flash(req, "error", "用户名 admin 为最高管理员保留账号");
            WebUtil.redirect(req, resp, "/admin");
            return;
        }
        if (existing != null && isSuperAdmin(existing)) {
            if (!isSuperAdmin(current)) {
                WebUtil.flash(req, "error", "只有最高管理员 admin 可以修改该账号");
                WebUtil.redirect(req, resp, "/admin");
                return;
            }
            if (!"ADMIN".equals(role)) {
                WebUtil.flash(req, "error", "最高管理员 admin 不能被降级");
                WebUtil.redirect(req, resp, "/admin");
                return;
            }
        }

        Map<String, String> errors = new LinkedHashMap<>();
        if (id == 0) {
            if (!USERNAME_PATTERN.matcher(username).matches()) {
                errors.put("username", "用户名需为 3-20 位字母、数字或下划线");
            } else if (userDao.existsByUsername(username)) {
                errors.put("username", "用户名已存在");
            }
            if (password.length() < 6 || password.length() > 32) {
                errors.put("password", "密码长度需为 6-32 位");
            }
        }
        if (nickname.isEmpty()) {
            errors.put("nickname", "请填写昵称");
        } else if (nickname.length() > 20) {
            errors.put("nickname", "昵称不能超过 20 个字符");
        }
        if (email.length() > 100) {
            errors.put("email", "邮箱不能超过 100 个字符");
        }

        if (id > 0 && existing == null) {
            errors.put("nickname", "用户不存在");
        }
        if (existing != null && existing.isAdmin() && !"ADMIN".equals(role) && userDao.countAdmins() <= 1) {
            errors.put("role", "不能取消最后一个管理员");
        }

        if (!errors.isEmpty()) {
            WebUtil.flash(req, "error", errors.values().iterator().next());
            WebUtil.redirect(req, resp, "/admin");
            return;
        }

        if (existing == null) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(PasswordUtil.hash(password));
            user.setNickname(nickname);
            user.setEmail(email);
            user.setRole(role);
            userDao.insert(user);
            WebUtil.flash(req, "success", "用户「" + nickname + "」已创建");
        } else {
            existing.setNickname(nickname);
            existing.setEmail(email);
            existing.setRole(role);
            userDao.update(existing);
            if (!password.isEmpty()) {
                if (password.length() < 6 || password.length() > 32) {
                    WebUtil.flash(req, "error", "密码长度需为 6-32 位，资料已保存");
                    WebUtil.redirect(req, resp, "/admin");
                    return;
                }
                userDao.updatePassword(id, PasswordUtil.hash(password));
            }
            WebUtil.flash(req, "success", "用户「" + nickname + "」已更新");
        }
        WebUtil.redirect(req, resp, "/admin");
    }

    private void deleteUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User current = (User) req.getSession().getAttribute("loginUser");
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        User target = id > 0 ? userDao.findById(id) : null;
        if (target == null) {
            WebUtil.flash(req, "error", "用户不存在");
        } else if (isSuperAdmin(target)) {
            WebUtil.flash(req, "error", "最高管理员 admin 不能被删除");
        } else if (id == current.getId()) {
            WebUtil.flash(req, "error", "不能删除当前登录的管理员");
        } else if (target.isAdmin() && userDao.countAdmins() <= 1) {
            WebUtil.flash(req, "error", "不能删除最后一个管理员");
        } else {
            // 解除该用户图书的上传者关联，避免历史数据悬空
            userDao.delete(id);
            WebUtil.flash(req, "success", "用户「" + target.getDisplayName() + "」已删除");
        }
        WebUtil.redirect(req, resp, "/admin");
    }

    private void review(HttpServletRequest req, HttpServletResponse resp, boolean approve) throws IOException {
        int bookId = WebUtil.intValue(req.getParameter("bookId"), 0);
        Book book = bookId > 0 ? bookDao.findById(bookId) : null;
        if (book == null) {
            WebUtil.flash(req, "error", "图书不存在或已被删除");
            WebUtil.redirect(req, resp, "/admin");
            return;
        }
        String reason = WebUtil.trim(req.getParameter("reason"));
        bookDao.setStatus(bookId, approve ? "APPROVED" : "REJECTED");
        if (book.getUploaderId() != null) {
            String message = approve
                    ? "你的作品《" + book.getName() + "》已通过审核，已上架到书架"
                    : "你的作品《" + book.getName() + "》未通过审核" + (reason.isEmpty() ? "" : "：" + reason);
            notificationDao.insert(book.getUploaderId(), approve ? "APPROVE" : "REJECT",
                    message, approve ? "/book?action=detail&id=" + bookId : "/book?action=myUploads");
        }
        WebUtil.flash(req, "success", "《" + book.getName() + "》" + (approve ? "已通过审核" : "已驳回"));
        WebUtil.redirect(req, resp, "/admin");
    }

    private boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = (User) req.getSession().getAttribute("loginUser");
        if (user != null && user.isAdmin()) {
            return true;
        }
        WebUtil.flash(req, "error", "该页面仅管理员可访问");
        WebUtil.redirect(req, resp, "/home");
        return false;
    }

    /** admin 是系统最高管理员账号。 */
    private boolean isSuperAdmin(User user) {
        return user != null && "admin".equalsIgnoreCase(user.getUsername());
    }
}
