package com.library.servlet;

import com.library.dao.BookDao;
import com.library.dao.BookshelfDao;
import com.library.entity.Book;
import com.library.entity.User;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 我的书架：查看收藏、加入 / 移出书架。
 */
public class BookshelfServlet extends HttpServlet {

    private final BookshelfDao bookshelfDao = new BookshelfDao();
    private final BookDao bookDao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("loginUser");
        if (user == null) {
            WebUtil.redirect(req, resp, "/login.jsp");
            return;
        }
        List<Book> books = bookshelfDao.findByUser(user.getId());
        req.setAttribute("shelfBooks", books);
        req.setAttribute("shelfCount", books.size());
        req.getRequestDispatcher("/WEB-INF/views/bookshelf.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User user = (User) req.getSession().getAttribute("loginUser");
        if (user == null) {
            WebUtil.redirect(req, resp, "/login.jsp");
            return;
        }

        String action = WebUtil.trim(req.getParameter("action"));
        int bookId = WebUtil.intValue(req.getParameter("bookId"), 0);
        Book book = bookId > 0 ? bookDao.findById(bookId) : null;
        if (book == null) {
            WebUtil.flash(req, "error", "图书不存在或已被删除");
            WebUtil.redirect(req, resp, "/bookshelf");
            return;
        }

        String next = safeNext(req.getParameter("next"), bookId);
        if ("remove".equals(action)) {
            bookshelfDao.remove(user.getId(), bookId);
            WebUtil.flash(req, "success", "《" + book.getName() + "》已移出书架");
        } else {
            if (bookshelfDao.exists(user.getId(), bookId)) {
                WebUtil.flash(req, "success", "《" + book.getName() + "》已经在书架里了");
            } else {
                bookshelfDao.add(user.getId(), bookId);
                WebUtil.flash(req, "success", "《" + book.getName() + "》已加入书架");
            }
        }
        WebUtil.redirect(req, resp, next);
    }

    /**
     * 只允许跳回站内相对路径，避免开放重定向。
     */
    private String safeNext(String next, int bookId) {
        if (next != null) {
            String path = next.trim();
            if (path.startsWith("/") && !path.startsWith("//") && !path.contains("\\")
                    && !path.contains("\r") && !path.contains("\n")) {
                return path;
            }
        }
        return "/book?action=detail&id=" + bookId;
    }
}
