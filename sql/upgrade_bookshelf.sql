-- ============================================================
--  墨卷书阁 · 我的书架升级脚本
--  适用：已有 library_db 的数据库，可重复执行
--  执行：mysql -uroot -p --default-character-set=utf8mb4 < sql\upgrade_bookshelf.sql
-- ============================================================

USE library_db;

CREATE TABLE IF NOT EXISTS bookshelf (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT      NOT NULL,
    book_id      INT      NOT NULL,
    last_chapter INT      NOT NULL DEFAULT 0 COMMENT '上次阅读章节，0 表示第一章',
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_read_at DATETIME DEFAULT NULL,
    UNIQUE KEY uk_bookshelf (user_id, book_id),
    KEY idx_bookshelf_user (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户书架';

SELECT '我的书架表已就绪' AS message,
       (SELECT COUNT(*) FROM bookshelf) AS shelf_items;
