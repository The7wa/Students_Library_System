package com.library.util;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 图书正文 TXT 的存放工具。
 *
 * <p>正文目录默认是 {@code 用户主目录/library-books}，因此重新部署或重启 Tomcat 都不会丢文件。
 * 可以用环境变量 {@code LIBRARY_BOOK_DIR} 或 JVM 参数 {@code -Dlibrary.book.dir=...} 覆盖。</p>
 */
public final class BookContentStore {

    /** 单个正文文件上限：20MB。 */
    public static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    /** 阅读时允许的最大文件：32MB，避免一次性读入过大文件。 */
    public static final long MAX_READ_SIZE = 32L * 1024 * 1024;

    private static final Pattern MANAGED_NAME = Pattern.compile("^[0-9a-f]{32}\\.txt$");

    private BookContentStore() {
    }

    /**
     * 正文目录，不存在时自动创建。
     */
    public static Path baseDir() {
        String dir = System.getProperty("library.book.dir");
        if (dir == null || dir.isBlank()) {
            dir = System.getenv("LIBRARY_BOOK_DIR");
        }
        if (dir == null || dir.isBlank()) {
            dir = Paths.get(System.getProperty("user.home"), "library-books").toString();
        }
        Path path = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(path);
        } catch (IOException ignored) {
            // 目录创建失败时，后续保存会给出明确错误。
        }
        return path;
    }

    /**
     * 把上传的 Part 保存到正文目录，返回存储文件名与原始文件名。
     */
    public static StoredFile store(Part part) throws IOException {
        String submitted = part.getSubmittedFileName();
        if (submitted == null || submitted.isBlank()) {
            throw new IOException("没有选择正文文件");
        }
        String original = Paths.get(submitted.replace('\\', '/')).getFileName().toString().trim();
        if (!isTxt(original)) {
            throw new IOException("正文只支持 .txt 文件");
        }
        if (part.getSize() <= 0) {
            throw new IOException("正文文件是空的");
        }
        if (part.getSize() > MAX_FILE_SIZE) {
            throw new IOException("正文文件不能超过 20MB");
        }
        if (original.length() > 160) {
            original = original.substring(0, 156) + ".txt";
        }

        String storedName = UUID.randomUUID().toString().replace("-", "") + ".txt";
        Path target = baseDir().resolve(storedName);
        try (InputStream in = part.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return new StoredFile(storedName, original);
    }

    /**
     * 把文件名解析为正文目录下的真实路径；越界或为空时返回 null。
     */
    public static Path resolve(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        Path base = baseDir();
        Path path = base.resolve(name).normalize();
        if (!path.startsWith(base) || !Files.isRegularFile(path)) {
            return null;
        }
        return path;
    }

    /**
     * 校验用户手填的正文文件名（例如直接放进正文目录的 活着.txt）。
     */
    public static String validateManualName(String name) {
        if (name == null || name.isBlank()) {
            return "请选择要上传的 TXT 文件";
        }
        if (name.contains("/") || name.contains("\\") || name.contains("..")) {
            return "文件名不能包含路径";
        }
        if (!isTxt(name)) {
            return "正文只支持 .txt 文件";
        }
        Path path = resolve(name);
        if (path == null) {
            return "正文目录中找不到这个文件，请先放进去";
        }
        try {
            if (Files.size(path) > MAX_FILE_SIZE) {
                return "正文文件不能超过 20MB";
            }
        } catch (IOException e) {
            return "无法读取正文文件";
        }
        return null;
    }

    /**
     * 只删除本系统上传生成的 UUID 文件，用户自己放进目录的 TXT 不会被误删。
     */
    public static void deleteManaged(String name) {
        if (name == null || !MANAGED_NAME.matcher(name).matches()) {
            return;
        }
        Path path = resolve(name);
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // 清理失败不影响业务。
            }
        }
    }

    public static boolean isTxt(String name) {
        return name != null && name.toLowerCase(Locale.ROOT).endsWith(".txt");
    }

    /**
     * 上传成功后返回的存储信息。
     */
    public static final class StoredFile {
        private final String storedName;
        private final String originalName;

        public StoredFile(String storedName, String originalName) {
            this.storedName = storedName;
            this.originalName = originalName;
        }

        public String getStoredName() {
            return storedName;
        }

        public String getOriginalName() {
            return originalName;
        }
    }
}
