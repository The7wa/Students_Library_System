package com.library.servlet;

import com.library.dao.BookDao;
import com.library.dao.BookshelfDao;
import com.library.dao.CategoryDao;
import com.library.dao.NotificationDao;
import com.library.dao.RatingDao;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.entity.User;
import com.library.util.BookContentStore;
import com.library.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 图书：上传（用户，需审核）、增删改（管理员）、详情、我的上传。
 *
 * <p>权限规则：</p>
 * <ul>
 *   <li>管理员：图书的新增、修改、删除、审核，全部允许，上架无需审核。</li>
 *   <li>普通用户：只能上传图书（进入待审核状态），可删除自己上传的图书。</li>
 * </ul>
 */
public class BookServlet extends HttpServlet {

    private final BookDao bookDao = new BookDao();
    private final BookshelfDao bookshelfDao = new BookshelfDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final NotificationDao notificationDao = new NotificationDao();
    private final RatingDao ratingDao = new RatingDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = WebUtil.trim(req.getParameter("action"));
        switch (action) {
            case "toAdd":
                showForm(req, resp, null, WebUtil.intValue(req.getParameter("categoryId"), 0));
                break;
            case "toEdit": {
                if (!requireAdmin(req, resp)) {
                    return;
                }
                Book book = findBook(req);
                if (book == null) {
                    WebUtil.redirect(req, resp, "/home");
                    return;
                }
                showForm(req, resp, book, book.getCategoryId());
                break;
            }
            case "detail": {
                Book book = findBook(req);
                User user = currentUser(req);
                if (book == null) {
                    WebUtil.flash(req, "error", "图书不存在或已被删除");
                    WebUtil.redirect(req, resp, "/home");
                    return;
                }
                boolean allowed = user.isAdmin()
                        || !book.isPending() && !book.isRejected()
                        || (book.getUploaderId() != null && book.getUploaderId() == user.getId());
                if (!allowed) {
                    WebUtil.flash(req, "error", "这本书还在审核中，暂时无法查看");
                    WebUtil.redirect(req, resp, "/home");
                    return;
                }
                req.setAttribute("book", book);
                req.setAttribute("inShelf", bookshelfDao.exists(user.getId(), book.getId()));
                req.setAttribute("myStars", ratingDao.findByUser(book.getId(), user.getId()));
                req.setAttribute("avgStars", book.getAvgStars());
                req.setAttribute("ratingCount", book.getRatingCount());
                req.getRequestDispatcher("/WEB-INF/views/book-detail.jsp").forward(req, resp);
                break;
            }
            case "myUploads": {
                User user = currentUser(req);
                List<Book> books = bookDao.findByUploader(user.getId());
                req.setAttribute("books", books);
                req.getRequestDispatcher("/WEB-INF/views/my-uploads.jsp").forward(req, resp);
                break;
            }
            default:
                WebUtil.redirect(req, resp, "/home");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if ("delete".equals(WebUtil.trim(req.getParameter("action")))) {
            delete(req, resp);
        } else {
            save(req, resp);
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, Book book, int categoryId)
            throws ServletException, IOException {
        if (book == null) {
            book = new Book();
            book.setCategoryId(categoryId);
            req.setAttribute("mode", "add");
        } else {
            req.setAttribute("mode", "edit");
        }
        req.setAttribute("book", book);
        req.setAttribute("categories", categoryDao.findAllWithCount());
        req.setAttribute("contentDir", BookContentStore.baseDir().toString());
        req.getRequestDispatcher("/WEB-INF/views/book-form.jsp").forward(req, resp);
    }

    private void save(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        Book existing = id > 0 ? bookDao.findById(id) : null;

        // 修改仅管理员；新增（上传）所有登录用户均可。
        if (id > 0 && !user.isAdmin()) {
            WebUtil.flash(req, "error", "只有管理员可以修改图书");
            WebUtil.redirect(req, resp, "/home");
            return;
        }

        Book book = new Book();
        book.setId(id);
        book.setCategoryId(WebUtil.intValue(req.getParameter("categoryId"), 0));
        book.setName(WebUtil.trim(req.getParameter("name")));
        book.setAuthor(WebUtil.trim(req.getParameter("author")));
        String isbnParam = req.getParameter("isbn");
        book.setIsbn(isbnParam == null && existing != null ? existing.getIsbn() : WebUtil.trim(isbnParam));
        book.setPublisher(WebUtil.trim(req.getParameter("publisher")));
        book.setDescription(WebUtil.trim(req.getParameter("description")));
        if (existing != null) {
            book.setContentFile(existing.getContentFile());
            book.setContentName(existing.getContentName());
            book.setUploaderId(existing.getUploaderId());
            // 管理员编辑被驳回的图书，视为重新通过审核
            book.setStatus(existing.isRejected() && user.isAdmin() ? "APPROVED" : existing.getStatus());
        } else {
            book.setUploaderId(user.getId());
            book.setStatus(user.isAdmin() ? "APPROVED" : "PENDING");
        }

        Map<String, String> errors = new LinkedHashMap<>();
        if (book.getName().isEmpty()) {
            errors.put("name", "请填写书名");
        } else if (book.getName().length() > 100) {
            errors.put("name", "书名不能超过 100 个字符");
        }
        if (book.getAuthor().isEmpty()) {
            errors.put("author", "请填写作者");
        } else if (book.getAuthor().length() > 50) {
            errors.put("author", "作者不能超过 50 个字符");
        }
        if (book.getIsbn().length() > 30) {
            errors.put("isbn", "ISBN 不能超过 30 个字符");
        }
        if (book.getPublisher().length() > 80) {
            errors.put("publisher", "出版社不能超过 80 个字符");
        }
        if (book.getDescription().length() > 2000) {
            errors.put("description", "简介不能超过 2000 个字符");
        }

        Category category = book.getCategoryId() > 0 ? categoryDao.findById(book.getCategoryId()) : null;
        if (category == null) {
            errors.put("categoryId", "请选择有效的板块");
        } else {
            book.setCategoryName(category.getName());
        }

        if (id > 0 && existing == null) {
            errors.put("name", "图书不存在或已被删除");
        }

        Part uploaded = null;
        if (isMultipart(req)) {
            try {
                uploaded = req.getPart("contentFile");
            } catch (IllegalStateException e) {
                errors.put("contentFile", "正文文件太大，单个文件不能超过 20MB");
            } catch (ServletException e) {
                errors.put("contentFile", "正文文件上传失败，请重新选择");
            }
        }
        if (uploaded != null && uploaded.getSize() <= 0) {
            String submittedName = uploaded.getSubmittedFileName();
            if (submittedName != null && !submittedName.trim().isEmpty()) {
                errors.put("contentFile", "正文文件是空的，请换一个 TXT");
            }
            uploaded = null;
        }

        String manualName = WebUtil.trim(req.getParameter("contentFileName"));
        boolean removeContent = "1".equals(WebUtil.trim(req.getParameter("removeContent")));

        if (uploaded != null) {
            if (!BookContentStore.isTxt(uploaded.getSubmittedFileName())) {
                errors.put("contentFile", "正文只支持 .txt 文件");
            } else if (uploaded.getSize() > BookContentStore.MAX_FILE_SIZE) {
                errors.put("contentFile", "正文文件不能超过 20MB");
            }
        } else if (!manualName.isEmpty()) {
            String manualError = BookContentStore.validateManualName(manualName);
            if (manualError != null) {
                errors.put("contentFileName", manualError);
            }
        }

        if (!errors.isEmpty()) {
            forwardForm(req, resp, book, id, errors);
            return;
        }

        try {
            if (uploaded != null) {
                BookContentStore.StoredFile stored = BookContentStore.store(uploaded);
                book.setContentFile(stored.getStoredName());
                book.setContentName(stored.getOriginalName());
            } else if (!manualName.isEmpty()) {
                book.setContentFile(manualName);
                book.setContentName(manualName);
            } else if (removeContent) {
                book.setContentFile(null);
                book.setContentName(null);
            }
        } catch (IOException e) {
            errors.put("contentFile", e.getMessage() == null ? "正文保存失败，请重试" : e.getMessage());
            forwardForm(req, resp, book, id, errors);
            return;
        }

        String redirectUrl;
        if (id > 0) {
            bookDao.update(book);
            WebUtil.flash(req, "success", "《" + book.getName() + "》已更新");
            redirectUrl = "/category?id=" + book.getCategoryId();
        } else {
            bookDao.insert(book);
            if (user.isAdmin()) {
                WebUtil.flash(req, "success", "《" + book.getName() + "》已上架");
                redirectUrl = "/category?id=" + book.getCategoryId();
            } else {
                WebUtil.flash(req, "success", "《" + book.getName() + "》已提交，等待管理员审核");
                notificationDao.notifyAdmins("UPLOAD",
                        user.getDisplayName() + " 上传了新书《" + book.getName() + "》，等待审核",
                        "/admin");
                redirectUrl = "/book?action=myUploads";
            }
        }

        if (existing != null) {
            String oldFile = existing.getContentFile();
            if (oldFile != null && !oldFile.equals(book.getContentFile())) {
                BookContentStore.deleteManaged(oldFile);
            }
        }
        WebUtil.redirect(req, resp, redirectUrl);
    }

    private void forwardForm(HttpServletRequest req, HttpServletResponse resp, Book book, int id,
                             Map<String, String> errors) throws ServletException, IOException {
        req.setAttribute("errors", errors);
        req.setAttribute("book", book);
        req.setAttribute("categories", categoryDao.findAllWithCount());
        req.setAttribute("contentDir", BookContentStore.baseDir().toString());
        req.setAttribute("mode", id > 0 ? "edit" : "add");
        req.getRequestDispatcher("/WEB-INF/views/book-form.jsp").forward(req, resp);
    }

    private boolean isMultipart(HttpServletRequest req) {
        String contentType = req.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = currentUser(req);
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        Book book = id > 0 ? bookDao.findById(id) : null;
        if (book == null) {
            WebUtil.flash(req, "error", "图书不存在或已被删除");
            WebUtil.redirect(req, resp, "/home");
            return;
        }
        // 管理员可删除任何图书；普通用户只能删除自己上传的。
        boolean own = book.getUploaderId() != null && book.getUploaderId() == user.getId();
        if (!user.isAdmin() && !own) {
            WebUtil.flash(req, "error", "只有管理员或上传者本人可以删除这本书");
            WebUtil.redirect(req, resp, "/category?id=" + book.getCategoryId());
            return;
        }
        bookDao.delete(id);
        BookContentStore.deleteManaged(book.getContentFile());
        WebUtil.flash(req, "success", "《" + book.getName() + "》已删除");
        if (!user.isAdmin()) {
            notificationDao.notifyAdmins("SYSTEM",
                    user.getDisplayName() + " 删除了自己上传的《" + book.getName() + "》", null);
            WebUtil.redirect(req, resp, "/book?action=myUploads");
            return;
        }
        if (own) {
            WebUtil.redirect(req, resp, "/book?action=myUploads");
        } else {
            WebUtil.redirect(req, resp, "/category?id=" + book.getCategoryId());
        }
    }

    private boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = currentUser(req);
        if (user.isAdmin()) {
            return true;
        }
        WebUtil.flash(req, "error", "该操作需要管理员权限");
        WebUtil.redirect(req, resp, "/home");
        return false;
    }

    private User currentUser(HttpServletRequest req) {
        return (User) req.getSession().getAttribute("loginUser");
    }

    private Book findBook(HttpServletRequest req) {
        int id = WebUtil.intValue(req.getParameter("id"), 0);
        return id > 0 ? bookDao.findById(id) : null;
    }
}
