package com.library.servlet;

import com.library.dao.NotificationDao;
import com.library.entity.Notification;
import com.library.entity.User;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 站内通知：列表、标记已读。
 */
public class NotificationServlet extends HttpServlet {

    private final NotificationDao notificationDao = new NotificationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("loginUser");
        String action = WebUtil.trim(req.getParameter("action"));
        if ("read".equals(action)) {
            int id = WebUtil.intValue(req.getParameter("id"), 0);
            if (id > 0) {
                notificationDao.markRead(id, user.getId());
            }
            String target = req.getParameter("target");
            if (target != null && target.startsWith("/") && !target.startsWith("//")) {
                WebUtil.redirect(req, resp, target);
            } else {
                WebUtil.redirect(req, resp, "/notification");
            }
            return;
        }
        List<Notification> list = notificationDao.findByUser(user.getId(), 100);
        req.setAttribute("notifications", list);
        req.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = (User) req.getSession().getAttribute("loginUser");
        String action = WebUtil.trim(req.getParameter("action"));
        if ("readAll".equals(action)) {
            notificationDao.markAllRead(user.getId());
            WebUtil.flash(req, "success", "所有通知已标记为已读");
        } else if ("clearAll".equals(action)) {
            int removed = notificationDao.deleteAllByUser(user.getId());
            WebUtil.flash(req, "success", "已清空 " + removed + " 条通知");
        }
        WebUtil.redirect(req, resp, "/notification");
    }
}
