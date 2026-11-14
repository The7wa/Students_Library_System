package com.library.servlet;

import com.library.dao.AnnotationCommentDao;
import com.library.dao.AnnotationDao;
import com.library.dao.AnnotationLikeDao;
import com.library.dao.BookDao;
import com.library.entity.Annotation;
import com.library.entity.AnnotationComment;
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
 * 阅读批注：保存 / 删除 / 点赞 / 评论 / 回复。
 *
 * <p>同一段原文重复标记只更新最后一条；评论对所有读者可见；note 只对本人展示。</p>
 */
public class AnnotationServlet extends HttpServlet {

    private static final Set<String> COLORS = new HashSet<>(Arrays.asList(
            "#f5c242", "#7bc77e", "#7fb2e5", "#e58fb1", "#b79ce0"));

    private final AnnotationDao annotationDao = new AnnotationDao();
    private final AnnotationCommentDao commentDao = new AnnotationCommentDao();
    private final AnnotationLikeDao likeDao = new AnnotationLikeDao();
    private final BookDao bookDao = new BookDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User user = (User) req.getSession().getAttribute("loginUser");
        String action = WebUtil.trim(req.getParameter("action"));

        if ("delete".equals(action)) {
            deleteAnnotation(req, resp, user);
        } else if ("like".equals(action)) {
            like(req, resp, user);
        } else if ("comment".equals(action) || "reply".equals(action)) {
            comment(req, resp, user, "reply".equals(action));
        } else if ("deleteComment".equals(action)) {
            deleteComment(req, resp, user);
        } else {
            save(req, resp, user);
        }
    }

    private void save(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int bookId = WebUtil.intValue(req.getParameter("bookId"), 0);
        int chapter = WebUtil.intValue(req.getParameter("chapter"), 0);
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
        annotationDao.upsert(a);

        WebUtil.flash(req, "success", "批注已保存；同一段内容再次标记会自动更新为最新颜色");
        WebUtil.redirect(req, resp, "/read?id=" + bookId + "&chapter=" + chapter + "#p-" + paragraphIndex);
    }

    private void deleteAnnotation(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        int bookId = WebUtil.intValue(req.getParameter("bookId"), 0);
        int chapter = WebUtil.intValue(req.getParameter("chapter"), 0);
        annotationDao.delete(id, user.getId());
        WebUtil.flash(req, "success", "批注已删除");
        WebUtil.redirect(req, resp, "/read?id=" + bookId + "&chapter=" + chapter);
    }

    private void like(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int annotationId = WebUtil.intValue(req.getParameter("annotationId"), 0);
        Annotation a = annotationId > 0 ? annotationDao.findById(annotationId) : null;
        if (a == null) {
            WebUtil.flash(req, "error", "批注不存在");
            WebUtil.redirect(req, resp, "/home");
            return;
        }
        boolean liked = likeDao.toggle(annotationId, user.getId());
        WebUtil.flash(req, "success", liked ? "已点赞这条批注" : "已取消点赞");
        WebUtil.redirect(req, resp, "/read?id=" + a.getBookId() + "&chapter=" + a.getChapterIndex() + "#anno-" + annotationId);
    }

    private void comment(HttpServletRequest req, HttpServletResponse resp, User user, boolean reply)
            throws IOException {
        int annotationId = WebUtil.intValue(req.getParameter("annotationId"), 0);
        Annotation a = annotationId > 0 ? annotationDao.findById(annotationId) : null;
        if (a == null) {
            WebUtil.flash(req, "error", "批注不存在");
            WebUtil.redirect(req, resp, "/home");
            return;
        }

        String content = WebUtil.trim(req.getParameter("content"));
        if (content.isEmpty()) {
            WebUtil.flash(req, "error", "评论内容不能为空");
            WebUtil.redirect(req, resp, "/read?id=" + a.getBookId() + "&chapter=" + a.getChapterIndex() + "#anno-" + annotationId);
            return;
        }
        if (content.length() > 1000) {
            content = content.substring(0, 1000);
        }

        Integer parentId = null;
        if (reply) {
            int pid = WebUtil.intValue(req.getParameter("parentId"), 0);
            AnnotationComment parent = pid > 0 ? commentDao.findById(pid) : null;
            if (parent == null || parent.getAnnotationId() != annotationId) {
                WebUtil.flash(req, "error", "回复的评论不存在");
                WebUtil.redirect(req, resp, "/read?id=" + a.getBookId() + "&chapter=" + a.getChapterIndex() + "#anno-" + annotationId);
                return;
            }
            // 只保留一层回复，回复回复时挂到根评论下
            parentId = parent.getParentId() == null ? parent.getId() : parent.getParentId();
        }

        AnnotationComment c = new AnnotationComment();
        c.setAnnotationId(annotationId);
        c.setUserId(user.getId());
        c.setParentId(parentId);
        c.setContent(content);
        commentDao.insert(c);

        WebUtil.flash(req, "success", reply ? "回复已发布" : "评论已发布");
        WebUtil.redirect(req, resp, "/read?id=" + a.getBookId() + "&chapter=" + a.getChapterIndex() + "#anno-" + annotationId);
    }

    private void deleteComment(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        AnnotationComment c = id > 0 ? commentDao.findById(id) : null;
        if (c == null) {
            WebUtil.flash(req, "error", "评论不存在");
            WebUtil.redirect(req, resp, "/home");
            return;
        }
        Annotation a = annotationDao.findById(c.getAnnotationId());
        boolean ok = commentDao.delete(id, user.getId(), user.isAdmin());
        WebUtil.flash(req, ok ? "success" : "error", ok ? "评论已删除" : "没有权限删除这条评论");
        int bookId = a == null ? 0 : a.getBookId();
        int chapter = a == null ? 0 : a.getChapterIndex();
        WebUtil.redirect(req, resp, "/read?id=" + bookId + "&chapter=" + chapter + "#anno-" + c.getAnnotationId());
    }
}
