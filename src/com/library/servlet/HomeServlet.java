package com.library.servlet;

import com.library.dao.BookDao;
import com.library.dao.CategoryDao;
import com.library.dao.UserDao;
import com.library.entity.Category;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 登录后的主页：板块总览 + 数据统计。
 */
public class HomeServlet extends HttpServlet {

    private final CategoryDao categoryDao = new CategoryDao();
    private final BookDao bookDao = new BookDao();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Category> categories = categoryDao.findAllWithCount();

        req.setAttribute("categories", categories);
        req.setAttribute("latestBooks", bookDao.findLatest(3));
        req.setAttribute("categoryCount", categories.size());
        req.setAttribute("titleCount", bookDao.countAll());
        req.setAttribute("readableCount", bookDao.countReadable());
        req.setAttribute("userCount", userDao.countAll());
        req.setAttribute("greeting", WebUtil.greeting());
        req.setAttribute("greetingEn", WebUtil.greetingEn());
        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }
}
