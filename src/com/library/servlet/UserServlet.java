package com.library.servlet;

import com.library.dao.AnnotationDao;
import com.library.dao.UserDao;
import com.library.entity.Annotation;
import com.library.entity.User;
import com.library.util.PasswordUtil;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 个人中心：个人主页、设置、我的笔记。
 */
public class UserServlet extends HttpServlet {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserDao userDao = new UserDao();
    private final AnnotationDao annotationDao = new AnnotationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User login = (User) req.getSession().getAttribute("loginUser");
        if (login == null) {
            WebUtil.redirect(req, resp, "/login.jsp");
            return;
        }
        User fresh = userDao.findById(login.getId());
        if (fresh == null) {
            req.getSession().invalidate();
            WebUtil.redirect(req, resp, "/login.jsp");
            return;
        }
        fresh.setPassword(null);
        req.getSession().setAttribute("loginUser", fresh);

        String action = WebUtil.trim(req.getParameter("action"));
        if ("settings".equals(action)) {
            req.getRequestDispatcher("/WEB-INF/views/settings.jsp").forward(req, resp);
            return;
        }
        if ("notes".equals(action)) {
            List<Annotation> notes = annotationDao.findLatestByUser(fresh.getId());
            req.setAttribute("notes", notes);
            req.setAttribute("noteCount", notes.size());
            req.getRequestDispatcher("/WEB-INF/views/my-notes.jsp").forward(req, resp);
            return;
        }

        List<Annotation> recentNotes = annotationDao.findLatestByUser(fresh.getId());
        req.setAttribute("profileUser", fresh);
        req.setAttribute("uploadCount", userDao.countUploads(fresh.getId()));
        req.setAttribute("annotationCount", userDao.countAnnotations(fresh.getId()));
        req.setAttribute("commentCount", userDao.countComments(fresh.getId()));
        req.setAttribute("ratingCount", userDao.countRatings(fresh.getId()));
        req.setAttribute("recentNotes", recentNotes);
        req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User login = (User) req.getSession().getAttribute("loginUser");
        if (login == null) {
            WebUtil.redirect(req, resp, "/login.jsp");
            return;
        }
        User fresh = userDao.findById(login.getId());
        if (fresh == null) {
            req.getSession().invalidate();
            WebUtil.redirect(req, resp, "/login.jsp");
            return;
        }
        fresh.setPassword(null);

        String action = WebUtil.trim(req.getParameter("action"));
        if ("updateProfile".equals(action)) {
            updateProfile(req, resp, fresh);
        } else if ("updatePassword".equals(action)) {
            updatePassword(req, resp, fresh);
        } else if ("language".equals(action)) {
            updateLanguage(req, resp, fresh);
        } else if ("deleteAccount".equals(action)) {
            deleteAccount(req, resp, fresh);
        } else {
            WebUtil.redirect(req, resp, "/user?action=profile");
        }
    }

    private void updateProfile(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {
        String nickname = WebUtil.trim(req.getParameter("nickname"));
        String email = WebUtil.trim(req.getParameter("email"));
        String bio = WebUtil.trim(req.getParameter("bio"));
        String avatar = WebUtil.trim(req.getParameter("avatar"));

        if (nickname.isEmpty() || nickname.length() > 20) {
            WebUtil.flash(req, "error", "昵称需为 1-20 个字符");
            WebUtil.redirect(req, resp, "/user?action=profile");
            return;
        }
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            WebUtil.flash(req, "error", "邮箱格式不正确");
            WebUtil.redirect(req, resp, "/user?action=profile");
            return;
        }
        if (bio.length() > 200) {
            WebUtil.flash(req, "error", "个人简介不能超过 200 个字符");
            WebUtil.redirect(req, resp, "/user?action=profile");
            return;
        }
        if (avatar.codePointCount(0, avatar.length()) > 8) {
            WebUtil.flash(req, "error", "头像格式不正确");
            WebUtil.redirect(req, resp, "/user?action=profile");
            return;
        }

        User update = new User();
        update.setId(user.getId());
        update.setNickname(nickname);
        update.setEmail(email);
        update.setBio(bio);
        update.setAvatar(avatar.isEmpty() ? null : avatar);
        userDao.updateProfile(update);

        User refreshed = userDao.findById(user.getId());
        refreshed.setPassword(null);
        req.getSession().setAttribute("loginUser", refreshed);
        WebUtil.flash(req, "success", "个人资料已更新");
        WebUtil.redirect(req, resp, "/user?action=profile");
    }

    private void updatePassword(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {
        String current = req.getParameter("currentPassword") == null ? "" : req.getParameter("currentPassword");
        String next = req.getParameter("newPassword") == null ? "" : req.getParameter("newPassword");
        String confirm = req.getParameter("confirmPassword") == null ? "" : req.getParameter("confirmPassword");

        User full = userDao.findById(user.getId());
        if (full == null || !PasswordUtil.verify(current, full.getPassword())) {
            WebUtil.flash(req, "error", "当前密码不正确");
            WebUtil.redirect(req, resp, "/user?action=settings");
            return;
        }
        if (next.length() < 6 || next.length() > 32) {
            WebUtil.flash(req, "error", "新密码长度需为 6-32 位");
            WebUtil.redirect(req, resp, "/user?action=settings");
            return;
        }
        if (!next.equals(confirm)) {
            WebUtil.flash(req, "error", "两次输入的新密码不一致");
            WebUtil.redirect(req, resp, "/user?action=settings");
            return;
        }
        userDao.updatePassword(user.getId(), PasswordUtil.hash(next));
        WebUtil.flash(req, "success", "密码已修改，下次登录请使用新密码");
        WebUtil.redirect(req, resp, "/user?action=settings");
    }

    private void updateLanguage(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {
        String lang = WebUtil.trim(req.getParameter("lang"));
        if (!"en".equals(lang) && !"zh".equals(lang)) {
            lang = "zh";
        }
        userDao.updateLanguage(user.getId(), lang);
        user.setLang(lang);
        req.getSession().setAttribute("loginUser", user);
        WebUtil.flash(req, "success", "界面语言已切换");
        WebUtil.redirect(req, resp, "/user?action=settings");
    }

    private void deleteAccount(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {
        if (user.isAdmin()) {
            WebUtil.flash(req, "error", "管理员账号不能注销；如需注销请先转移管理员权限");
            WebUtil.redirect(req, resp, "/user?action=settings");
            return;
        }
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        User full = userDao.findById(user.getId());
        if (full == null || !PasswordUtil.verify(password, full.getPassword())) {
            WebUtil.flash(req, "error", "密码不正确，无法注销账号");
            WebUtil.redirect(req, resp, "/user?action=settings");
            return;
        }
        userDao.delete(user.getId());
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        WebUtil.redirect(req, resp, "/login.jsp?deleted=1");
    }
}
