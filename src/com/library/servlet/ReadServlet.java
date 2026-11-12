package com.library.servlet;

import com.library.dao.BookDao;
import com.library.entity.Book;
import com.library.entity.Chapter;
import com.library.util.BookContentStore;
import com.library.util.TxtBookParser;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 在线阅读：读取图书关联的 TXT，自动分章后渲染阅读页。
 */
public class ReadServlet extends HttpServlet {

    private final BookDao bookDao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        Book book = id > 0 ? bookDao.findById(id) : null;
        if (book == null) {
            WebUtil.flash(req, "error", "图书不存在或已被删除");
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

        req.setAttribute("book", book);
        req.setAttribute("chapters", chapters);
        req.setAttribute("chapter", chapters.get(index));
        req.setAttribute("chapterIndex", index);
        req.setAttribute("chapterCount", chapters.size());
        req.setAttribute("hasPrev", index > 0);
        req.setAttribute("hasNext", index < chapters.size() - 1);
        req.getRequestDispatcher("/WEB-INF/views/reader.jsp").forward(req, resp);
    }
}
