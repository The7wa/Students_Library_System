package com.library.servlet;

import com.library.dao.BookDao;
import com.library.dao.CategoryDao;
import com.library.entity.Book;
import com.library.entity.Page;
import com.library.util.SearchHighlighter;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 全馆搜索。
 */
public class SearchServlet extends HttpServlet {

    /** 全部图书页一页展示完，滚动到底即可，无翻页。 */
    private static final int PAGE_SIZE = 500;

    private final BookDao bookDao = new BookDao();
    private final CategoryDao categoryDao = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = WebUtil.trim(req.getParameter("keyword"));
        String sort = WebUtil.trim(req.getParameter("sort"));
        int pageNo = WebUtil.intValue(req.getParameter("page"), 1);

        Page<Book> bookPage = bookDao.search(null, keyword, sort, pageNo, PAGE_SIZE);
        // 搜索关键字为空时，显示提示信息而不是"没有找到这本书"
        if (bookPage.getItems().isEmpty() && (keyword == null || keyword.trim().isEmpty())) {
            req.setAttribute("emptySearch", true);
        }
        for (Book book : bookPage.getItems()) {
            book.setSearchNameHtml(SearchHighlighter.highlight(book.getName(), keyword));
            book.setSearchAuthorHtml(SearchHighlighter.highlight(book.getAuthor(), keyword));
            book.setSearchPublisherHtml(SearchHighlighter.highlight(book.getPublisher(), keyword));
            String source = book.getDescription();
            if (source == null || source.isBlank()) {
                source = book.getName() + " · " + book.getAuthor();
            }
            book.setSearchSnippetHtml(SearchHighlighter.snippet(source, keyword, 36));
        }

        req.setAttribute("bookPage", bookPage);
        req.setAttribute("categories", categoryDao.findAllWithCount());
        req.setAttribute("keyword", keyword);
        req.setAttribute("sort", sort);
        req.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(req, resp);
    }
}
