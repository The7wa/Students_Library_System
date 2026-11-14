package com.library.servlet;

import com.library.dao.NotificationDao;
import com.library.dao.UserDao;
import com.library.entity.User;
import com.library.util.PasswordUtil;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 登录 / 注册 / 退出。
 */
public class AuthServlet extends HttpServlet {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserDao userDao = new UserDao();
    private final NotificationDao notificationDao = new NotificationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("logout".equals(req.getParameter("action"))) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            WebUtil.redirect(req, resp, "/login.jsp?logout=1");
            return;
        }
        WebUtil.redirect(req, resp, "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = WebUtil.trim(req.getParameter("action"));
        if ("register".equals(action)) {
            register(req, resp);
        } else {
            login(req, resp);
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = WebUtil.trim(req.getParameter("username"));
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");

        Map<String, String> errors = new LinkedHashMap<>();
        if (username.isEmpty()) {
            errors.put("username", "请输入用户名");
        }
        if (password.isEmpty()) {
            errors.put("password", "请输入密码");
        }

        User user = null;
        if (errors.isEmpty()) {
            user = userDao.findByUsername(username);
            if (user == null || !PasswordUtil.verify(password, user.getPassword())) {
                errors.put("login", "用户名或密码错误，请重新输入");
            }
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("username", username);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        startSession(req, user);
        WebUtil.flash(req, "success", "欢迎回来，" + user.getDisplayName());
        WebUtil.redirect(req, resp, "/home");
    }

    private void register(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = WebUtil.trim(req.getParameter("username"));
        String nickname = WebUtil.trim(req.getParameter("nickname"));
        String email = WebUtil.trim(req.getParameter("email"));
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword") == null
                ? "" : req.getParameter("confirmPassword");

        Map<String, String> errors = new LinkedHashMap<>();

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            errors.put("username", "用户名需为 3-20 位字母、数字或下划线");
        }
        if (nickname.isEmpty()) {
            errors.put("nickname", "请输入昵称");
        } else if (nickname.length() > 20) {
            errors.put("nickname", "昵称不能超过 20 个字符");
        }
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            errors.put("email", "邮箱格式不正确");
        }
        if (password.length() < 6 || password.length() > 32) {
            errors.put("password", "密码长度需为 6-32 位");
        }
        if (!password.equals(confirmPassword)) {
            errors.put("confirmPassword", "两次输入的密码不一致");
        }
        if (errors.isEmpty() && userDao.existsByUsername(username)) {
            errors.put("username", "该用户名已被注册，换一个试试");
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("username", username);
            req.setAttribute("nickname", nickname);
            req.setAttribute("email", email);
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.hash(password));
        user.setNickname(nickname);
        user.setEmail(email);
        user.setRole("USER");
        userDao.insert(user);
        notificationDao.notifyAdmins("USER",
                "新用户注册：" + user.getDisplayName() + "（" + user.getUsername() + "）", "/admin");

        startSession(req, user);
        WebUtil.flash(req, "success", "注册成功，欢迎加入墨卷书阁，" + nickname + "！");
        WebUtil.redirect(req, resp, "/home");
    }

    private void startSession(HttpServletRequest req, User user) {
        HttpSession session = req.getSession(true);
        req.changeSessionId();
        user.setPassword(null);
        session.setAttribute("loginUser", user);
        session.setMaxInactiveInterval(60 * 60);
    }
}
