package com.library.servlet;

import com.library.dao.BookDao;
import com.library.dao.RatingDao;
import com.library.entity.Book;
import com.library.entity.User;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 图书打星（1-5 星，每人每书一票，可修改）。
 */
public class RatingServlet extends HttpServlet {

    private final RatingDao ratingDao = new RatingDao();
    private final BookDao bookDao = new BookDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("loginUser");
        int bookId = WebUtil.intValue(req.getParameter("bookId"), 0);
        int stars = WebUtil.intValue(req.getParameter("stars"), 0);
        Book book = bookId > 0 ? bookDao.findById(bookId) : null;
        if (book == null) {
            WebUtil.flash(req, "error", "图书不存在");
            WebUtil.redirect(req, resp, "/home");
            return;
        }
        if (stars < 1 || stars > 5) {
            WebUtil.flash(req, "error", "请选择 1-5 星");
        } else {
            ratingDao.rate(bookId, user.getId(), stars);
            WebUtil.flash(req, "success", "评分成功：" + stars + " 星");
        }
        WebUtil.redirect(req, resp, "/book?action=detail&id=" + bookId);
    }
}
