package com.library.servlet;

import com.library.dao.AnnotationCommentDao;
import com.library.dao.AnnotationDao;
import com.library.dao.BookDao;
import com.library.dao.CommentDao;
import com.library.dao.RatingDao;
import com.library.entity.Annotation;
import com.library.entity.AnnotationComment;
import com.library.entity.Book;
import com.library.entity.Chapter;
import com.library.entity.Comment;
import com.library.entity.User;
import com.library.util.BookContentStore;
import com.library.util.TxtBookParser;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 在线阅读：正文分章、分段落渲染，并加载批注 / 评论 / 评分。
 */
public class ReadServlet extends HttpServlet {

    private final BookDao bookDao = new BookDao();
    private final AnnotationDao annotationDao = new AnnotationDao();
    private final AnnotationCommentDao annotationCommentDao = new AnnotationCommentDao();
    private final CommentDao commentDao = new CommentDao();
    private final RatingDao ratingDao = new RatingDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("loginUser");
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        Book book = id > 0 ? bookDao.findById(id) : null;
        if (book == null) {
            WebUtil.flash(req, "error", "图书不存在或已被删除");
            WebUtil.redirect(req, resp, "/home");
            return;
        }
        boolean allowed = user.isAdmin() || "APPROVED".equalsIgnoreCase(book.getStatus())
                || (book.getUploaderId() != null && book.getUploaderId() == user.getId());
        if (!allowed) {
            WebUtil.flash(req, "error", "这本书还在审核中，暂时无法阅读");
            WebUtil.redirect(req, resp, "/home");
            return;
        }

        String detailUrl = "/book?action=detail&id=" + book.getId();
        if (!book.isHasContent()) {
            WebUtil.flash(req, "error", "《" + book.getName() + "》还没有上传正文 TXT");
            WebUtil.redirect(req, resp, detailUrl);
            return;
        }

        Path file = BookContentStore.resolve(book.getContentFile());
        if (file == null) {
            WebUtil.flash(req, "error", "正文文件不存在，请重新上传");
            WebUtil.redirect(req, resp, detailUrl);
            return;
        }

        List<Chapter> chapters;
        try {
            chapters = TxtBookParser.parse(file);
        } catch (IOException e) {
            WebUtil.flash(req, "error", "正文读取失败：" + e.getMessage());
            WebUtil.redirect(req, resp, detailUrl);
            return;
        }
        if (chapters.isEmpty()) {
            WebUtil.flash(req, "error", "正文内容为空，换一个 TXT 文件试试");
            WebUtil.redirect(req, resp, detailUrl);
            return;
        }

        int index = WebUtil.intValue(req.getParameter("chapter"), 0);
        index = Math.max(0, Math.min(index, chapters.size() - 1));
        Chapter chapter = chapters.get(index);

        List<String> paragraphs = new ArrayList<>();
        for (String line : chapter.getContent().split("\n")) {
            String text = line.trim();
            if (!text.isEmpty()) {
                paragraphs.add(text);
            }
        }
        if (paragraphs.isEmpty()) {
            paragraphs.add(chapter.getContent());
        }

        List<Annotation> annotations = annotationDao.findByBookAndUser(book.getId(), user.getId());
        List<Annotation> publicAnnotations = annotationDao.findByBookAndChapter(book.getId(), index, user.getId());
        List<AnnotationComment> annotationComments =
                annotationCommentDao.findByBookAndChapter(book.getId(), index, user.getId());

        Map<Integer, List<AnnotationComment>> commentsByAnnotation = new LinkedHashMap<>();
        for (Annotation a : publicAnnotations) {
            commentsByAnnotation.put(a.getId(), new ArrayList<>());
        }
        for (AnnotationComment ac : annotationComments) {
            commentsByAnnotation.computeIfAbsent(ac.getAnnotationId(), k -> new ArrayList<>()).add(ac);
        }

        List<Comment> comments = commentDao.findByBookAndChapter(book.getId(), index);

        req.setAttribute("book", book);
        req.setAttribute("chapters", chapters);
        req.setAttribute("chapter", chapter);
        req.setAttribute("paragraphs", paragraphs);
        req.setAttribute("chapterIndex", index);
        req.setAttribute("chapterCount", chapters.size());
        req.setAttribute("hasPrev", index > 0);
        req.setAttribute("hasNext", index < chapters.size() - 1);
        req.setAttribute("annotations", annotations);
        req.setAttribute("publicAnnotations", publicAnnotations);
        req.setAttribute("commentsByAnnotation", commentsByAnnotation);
        req.setAttribute("annotationsJson", toJson(annotations));
        req.setAttribute("comments", comments);
        req.setAttribute("myStars", ratingDao.findByUser(book.getId(), user.getId()));
        req.setAttribute("avgStars", book.getAvgStars());
        req.setAttribute("ratingCount", book.getRatingCount());
        req.getRequestDispatcher("/WEB-INF/views/reader.jsp").forward(req, resp);
    }

    /**
     * 把当前用户的批注转换为 JSON，供前端定位与高亮。
     */
    private String toJson(List<Annotation> annotations) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < annotations.size(); i++) {
            Annotation a = annotations.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append('{')
                    .append("\"id\":").append(a.getId()).append(',')
                    .append("\"chapter\":").append(a.getChapterIndex()).append(',')
                    .append("\"p\":").append(a.getParagraphIndex()).append(',')
                    .append("\"quote\":\"").append(escape(a.getQuote())).append("\",")
                    .append("\"note\":\"").append(escape(a.getNote())).append("\",")
                    .append("\"color\":\"").append(escape(a.getColor())).append("\"")
                    .append('}');
        }
        return sb.append(']').toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '<': sb.append("\\u003c"); break;
                case '>': sb.append("\\u003e"); break;
                case '&': sb.append("\\u0026"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}
