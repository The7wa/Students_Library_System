package com.library.filter;

import com.library.dao.BookDao;
import com.library.dao.NotificationDao;
import com.library.entity.User;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 为页面准备通知角标和（管理员）待审核数量。
 */
public class NotificationFilter implements Filter {

    private final NotificationDao notificationDao = new NotificationDao();
    private final BookDao bookDao = new BookDao();

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI().substring(req.getContextPath().length());
        User user = req.getSession(false) == null
                ? null : (User) req.getSession(false).getAttribute("loginUser");
        if (user != null && !path.startsWith("/assets/")) {
            req.setAttribute("unreadNotifCount", notificationDao.unreadCount(user.getId()));
            if (user.isAdmin()) {
                req.setAttribute("pendingBookCount", bookDao.countPending());
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
