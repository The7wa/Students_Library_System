package com.library.servlet;

import com.library.dao.BookDao;
import com.library.dao.CategoryDao;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.entity.Page;
import com.library.entity.User;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 板块：主页卡片进入书架、板块增删改。
 */
public class CategoryServlet extends HttpServlet {

    private static final int PAGE_SIZE = 12;
    private static final Set<String> COLORS = new HashSet<>(Arrays.asList(
            "clay", "forest", "ocean", "plum", "amber", "slate", "rose", "teal"));

    private final CategoryDao categoryDao = new CategoryDao();
    private final BookDao bookDao = new BookDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = WebUtil.trim(req.getParameter("action"));
        switch (action) {
            case "toAdd":
                if (!requireAdmin(req, resp)) {
                    return;
                }
                showForm(req, resp, null);
                break;
            case "toEdit":
                if (!requireAdmin(req, resp)) {
                    return;
                }
                showForm(req, resp, findCategory(req));
                break;
            default:
                shelf(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!requireAdmin(req, resp)) {
            return;
        }
        String action = WebUtil.trim(req.getParameter("action"));
        if ("delete".equals(action)) {
            delete(req, resp);
        } else {
            save(req, resp);
        }
    }

    private void shelf(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        Category category = id > 0 ? categoryDao.findById(id) : null;
        if (category == null) {
            WebUtil.flash(req, "error", "板块不存在或已被删除");
            WebUtil.redirect(req, resp, "/home");
            return;
        }

        String keyword = WebUtil.trim(req.getParameter("keyword"));
        String sort = WebUtil.trim(req.getParameter("sort"));
        int pageNo = WebUtil.intValue(req.getParameter("page"), 1);
        Page<Book> bookPage = bookDao.search(category.getId(), keyword, sort, pageNo, PAGE_SIZE);

        req.setAttribute("category", category);
        req.setAttribute("bookPage", bookPage);
        req.setAttribute("categories", categoryDao.findAllWithCount());
        req.setAttribute("keyword", keyword);
        req.setAttribute("sort", sort);
        req.setAttribute("activeCategoryId", category.getId());
        req.getRequestDispatcher("/WEB-INF/views/shelf.jsp").forward(req, resp);
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, Category category)
            throws ServletException, IOException {
        if (category == null && "toEdit".equals(req.getParameter("action"))) {
            WebUtil.flash(req, "error", "要编辑的板块不存在");
            WebUtil.redirect(req, resp, "/home");
            return;
        }
        if (category == null) {
            category = new Category();
            category.setIcon("📚");
            category.setColor("clay");
            req.setAttribute("mode", "add");
        } else {
            req.setAttribute("mode", "edit");
        }
        req.setAttribute("category", category);
        req.getRequestDispatcher("/WEB-INF/views/category-form.jsp").forward(req, resp);
    }

    private void save(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        Category category = new Category();
        category.setId(id);
        category.setName(WebUtil.trim(req.getParameter("name")));
        category.setIcon(WebUtil.trim(req.getParameter("icon")));
        category.setDescription(WebUtil.trim(req.getParameter("description")));
        category.setColor(WebUtil.trim(req.getParameter("color")));
        category.setSortOrder(WebUtil.intValue(req.getParameter("sortOrder"), 0));

        Map<String, String> errors = new LinkedHashMap<>();
        if (category.getName().isEmpty()) {
            errors.put("name", "请填写板块名称");
        } else if (category.getName().length() > 30) {
            errors.put("name", "板块名称不能超过 30 个字符");
        } else if (categoryDao.existsByName(category.getName(), id)) {
            errors.put("name", "已存在同名板块");
        }
        if (category.getIcon().isEmpty()) {
            category.setIcon("📚");
        } else if (category.getIcon().codePointCount(0, category.getIcon().length()) > 4) {
            errors.put("icon", "图标建议只填 1 个 emoji");
        }
        if (category.getDescription().length() > 120) {
            errors.put("description", "板块简介不能超过 120 个字符");
        }
        if (!COLORS.contains(category.getColor())) {
            category.setColor("clay");
        }
        if (id > 0 && categoryDao.findById(id) == null) {
            errors.put("name", "板块不存在或已被删除");
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("category", category);
            req.setAttribute("mode", id > 0 ? "edit" : "add");
            req.getRequestDispatcher("/WEB-INF/views/category-form.jsp").forward(req, resp);
            return;
        }

        if (id > 0) {
            categoryDao.update(category);
            WebUtil.flash(req, "success", "板块「" + category.getName() + "」已更新");
        } else {
            categoryDao.insert(category);
            WebUtil.flash(req, "success", "板块「" + category.getName() + "」创建成功");
        }
        WebUtil.redirect(req, resp, "/category?id=" + category.getId());
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        Category category = id > 0 ? categoryDao.findById(id) : null;
        if (category == null) {
            WebUtil.flash(req, "error", "板块不存在或已被删除");
            WebUtil.redirect(req, resp, "/home");
            return;
        }
        categoryDao.delete(id);
        WebUtil.flash(req, "success",
                "板块「" + category.getName() + "」已删除，其中 " + category.getBookCount() + " 本图书一并移除");
        WebUtil.redirect(req, resp, "/home");
    }

    private Category findCategory(HttpServletRequest req) {
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        return id > 0 ? categoryDao.findById(id) : null;
    }

    private boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = (User) req.getSession().getAttribute("loginUser");
        if (user != null && user.isAdmin()) {
            return true;
        }
        WebUtil.flash(req, "error", "该操作需要管理员权限");
        WebUtil.redirect(req, resp, "/home");
        return false;
    }
}
