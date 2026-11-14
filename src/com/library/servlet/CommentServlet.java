package com.library.servlet;

import com.library.dao.BookDao;
import com.library.dao.CommentDao;
import com.library.dao.NotificationDao;
import com.library.entity.Book;
import com.library.entity.Comment;
import com.library.entity.User;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 章节评论：所有人可见；本人可删自己的，管理员可删任何人的。
 */
public class CommentServlet extends HttpServlet {

    private final CommentDao commentDao = new CommentDao();
    private final BookDao bookDao = new BookDao();
    private final NotificationDao notificationDao = new NotificationDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User user = (User) req.getSession().getAttribute("loginUser");
        String action = WebUtil.trim(req.getParameter("action"));
        int bookId = WebUtil.intValue(req.getParameter("bookId"), 0);
        int chapter = WebUtil.intValue(req.getParameter("chapter"), 0);

        if ("delete".equals(action)) {
            int id = WebUtil.intValue(req.getParameter("id"), 0);
            Comment comment = id > 0 ? commentDao.findById(id) : null;
            if (comment == null) {
                WebUtil.flash(req, "error", "评论不存在");
            } else if (!user.isAdmin() && comment.getUserId() != user.getId()) {
                WebUtil.flash(req, "error", "只能删除自己的评论");
            } else {
                commentDao.delete(id);
                bookId = comment.getBookId();
                chapter = comment.getChapterIndex();
                WebUtil.flash(req, "success", "评论已删除");
            }
            WebUtil.redirect(req, resp, "/read?id=" + bookId + "&chapter=" + chapter + "#comments");
            return;
        }

        Book book = bookId > 0 ? bookDao.findById(bookId) : null;
        if (book == null) {
            WebUtil.flash(req, "error", "图书不存在");
            WebUtil.redirect(req, resp, "/home");
            return;
        }
        String content = WebUtil.trim(req.getParameter("content"));
        if (content.isEmpty()) {
            WebUtil.flash(req, "error", "评论内容不能为空");
        } else if (content.length() > 1000) {
            WebUtil.flash(req, "error", "评论不能超过 1000 字");
        } else {
            Comment c = new Comment();
            c.setBookId(bookId);
            c.setChapterIndex(Math.max(0, chapter));
            c.setUserId(user.getId());
            c.setContent(content);
            commentDao.insert(c);
            notificationDao.notifyAdmins("COMMENT",
                    user.getDisplayName() + " 评论了《" + book.getName() + "》第 " + (chapter + 1) + " 章",
                    "/read?id=" + bookId + "&chapter=" + chapter + "#comments");
            WebUtil.flash(req, "success", "评论已发表");
        }
        WebUtil.redirect(req, resp, "/read?id=" + bookId + "&chapter=" + chapter + "#comments");
    }
}
