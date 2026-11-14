package com.library.servlet;

import com.library.dao.BookDao;
import com.library.dao.ParagraphCommentDao;
import com.library.entity.Book;
import com.library.entity.ParagraphComment;
import com.library.entity.User;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 段落公开评论：所有人可见，支持评论和一层回复。
 * 与私有批注完全分开。
 */
public class ParagraphCommentServlet extends HttpServlet {

    private final ParagraphCommentDao commentDao = new ParagraphCommentDao();
    private final BookDao bookDao = new BookDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User user = (User) req.getSession().getAttribute("loginUser");
        if (user == null) {
            WebUtil.redirect(req, resp, "/login.jsp");
            return;
        }

        int bookId = WebUtil.intValue(req.getParameter("bookId"), 0);
        int chapter = Math.max(0, WebUtil.intValue(req.getParameter("chapter"), 0));
        int paragraph = WebUtil.intValue(req.getParameter("paragraph"), 0);
        String quote = WebUtil.trim(req.getParameter("quote"));
        String content = WebUtil.trim(req.getParameter("content"));
        String action = WebUtil.trim(req.getParameter("action"));

        Book book = bookId > 0 ? bookDao.findById(bookId) : null;
        if (book == null) {
            WebUtil.flash(req, "error", "图书不存在");
            WebUtil.redirect(req, resp, "/home");
            return;
        }
        if (paragraph < 0) {
            paragraph = 0;
        }
        if (quote.length() > 500) {
            quote = quote.substring(0, 500);
        }
        if (content.isEmpty()) {
            WebUtil.flash(req, "error", "评论内容不能为空");
            WebUtil.redirect(req, resp, "/read?id=" + bookId + "&chapter=" + chapter + "#comments-p-" + paragraph);
            return;
        }
        if (content.length() > 1000) {
            content = content.substring(0, 1000);
        }

        Integer parentId = null;
        if ("reply".equals(action)) {
            int pid = WebUtil.intValue(req.getParameter("parentId"), 0);
            ParagraphComment parent = pid > 0 ? commentDao.findById(pid, user.getId()) : null;
            if (parent == null || parent.getBookId() != bookId
                    || parent.getChapterIndex() != chapter
                    || parent.getParagraphIndex() != paragraph) {
                WebUtil.flash(req, "error", "要回复的评论不存在");
                WebUtil.redirect(req, resp, "/read?id=" + bookId + "&chapter=" + chapter + "#comments-p-" + paragraph);
                return;
            }
            // 只保留一层回复
            parentId = parent.getParentId() == null ? parent.getId() : parent.getParentId();
        }

        ParagraphComment c = new ParagraphComment();
        c.setBookId(bookId);
        c.setChapterIndex(chapter);
        c.setParagraphIndex(paragraph);
        c.setQuote(quote);
        c.setUserId(user.getId());
        c.setParentId(parentId);
        c.setContent(content);
        commentDao.insert(c);

        WebUtil.flash(req, "success", "评论已发布");
        WebUtil.redirect(req, resp, "/read?id=" + bookId + "&chapter=" + chapter + "#comments-p-" + paragraph);
    }
}
