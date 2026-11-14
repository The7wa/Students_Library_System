package com.library.servlet;

import com.library.dao.AnnotationDao;
import com.library.dao.BookDao;
import com.library.entity.Annotation;
import com.library.entity.Book;
import com.library.entity.User;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 阅读批注：新增 / 删除（仅本人可见）。
 */
public class AnnotationServlet extends HttpServlet {

    private static final Set<String> COLORS = new HashSet<>(Arrays.asList(
            "#f5c242", "#7bc77e", "#7fb2e5", "#e58fb1", "#b79ce0"));

    private final AnnotationDao annotationDao = new AnnotationDao();
    private final BookDao bookDao = new BookDao();

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
            annotationDao.delete(id, user.getId());
            WebUtil.flash(req, "success", "批注已删除");
            WebUtil.redirect(req, resp, "/read?id=" + bookId + "&chapter=" + chapter);
            return;
        }

        Book book = bookId > 0 ? bookDao.findById(bookId) : null;
        if (book == null) {
            WebUtil.flash(req, "error", "图书不存在");
            WebUtil.redirect(req, resp, "/home");
            return;
        }

        int paragraphIndex = WebUtil.intValue(req.getParameter("p"), 0);
        String quote = WebUtil.trim(req.getParameter("quote"));
        String note = WebUtil.trim(req.getParameter("note"));
        String color = WebUtil.trim(req.getParameter("color"));
        if (!COLORS.contains(color)) {
            color = "#f5c242";
        }

        String error = null;
        if (quote.isEmpty()) {
            error = "请先选中要批注的原文";
        } else if (quote.length() > 500) {
            error = "选中的原文太长，请分段批注";
        } else if (note.length() > 1000) {
            error = "批注内容不能超过 1000 字";
        } else if (paragraphIndex < 0) {
            error = "批注位置无效";
        }

        if (error != null) {
            WebUtil.flash(req, "error", error);
            WebUtil.redirect(req, resp, "/read?id=" + bookId + "&chapter=" + chapter);
            return;
        }

        Annotation a = new Annotation();
        a.setBookId(bookId);
        a.setUserId(user.getId());
        a.setChapterIndex(Math.max(0, chapter));
        a.setParagraphIndex(paragraphIndex);
        a.setQuote(quote);
        a.setNote(note);
        a.setColor(color);
        annotationDao.insert(a);
        WebUtil.flash(req, "success", "批注已保存");
        WebUtil.redirect(req, resp, "/read?id=" + bookId + "&chapter=" + chapter + "#p-" + paragraphIndex);
    }
}
