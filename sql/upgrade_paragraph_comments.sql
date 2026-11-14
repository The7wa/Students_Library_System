-- ============================================================
--  墨卷书阁 · 段落公开评论升级脚本
--  适用：已有 library_db 的数据库，可重复执行
--  执行：mysql -uroot -p --default-character-set=utf8mb4 < sql\upgrade_paragraph_comments.sql
-- ============================================================

USE library_db;

CREATE TABLE IF NOT EXISTS paragraph_comment (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    book_id         INT           NOT NULL,
    chapter_index   INT           NOT NULL,
    paragraph_index INT           NOT NULL,
    quote           VARCHAR(500)  DEFAULT NULL COMMENT '选中原文，可为空',
    user_id         INT           NOT NULL,
    parent_id       INT           DEFAULT NULL COMMENT '回复的父评论 id',
    content         VARCHAR(1000) NOT NULL,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_pcomment_target (book_id, chapter_index, paragraph_index, created_at),
    KEY idx_pcomment_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='段落公开评论';

SELECT '段落公开评论表已就绪' AS message,
       (SELECT COUNT(*) FROM paragraph_comment) AS comments;
