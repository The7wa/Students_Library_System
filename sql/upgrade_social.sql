-- ============================================================
--  墨卷书阁 · 权限 / 审核 / 社交功能升级脚本
--  适用：已执行过 library_db.sql 的数据库，可重复执行
--  执行：mysql -uroot -p --default-character-set=utf8mb4 < sql\upgrade_social.sql
-- ============================================================

USE library_db;

-- book 表新增：上传者、审核状态（可重复执行）
SET @add_uploader = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE book ADD COLUMN uploader_id INT DEFAULT NULL COMMENT ''上传者'' AFTER content_name',
        'SELECT ''uploader_id 已存在，跳过'' AS message')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'book' AND COLUMN_NAME = 'uploader_id'
);
PREPARE stmt_uploader FROM @add_uploader;
EXECUTE stmt_uploader;
DEALLOCATE PREPARE stmt_uploader;

SET @add_status = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE book ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT ''APPROVED'' COMMENT ''APPROVED/PENDING/REJECTED'' AFTER uploader_id',
        'SELECT ''status 已存在，跳过'' AS message')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'book' AND COLUMN_NAME = 'status'
);
PREPARE stmt_status FROM @add_status;
EXECUTE stmt_status;
DEALLOCATE PREPARE stmt_status;

-- 旧数据全部视为已上架，上传者记为管理员
UPDATE book
SET status = 'APPROVED',
    uploader_id = COALESCE(uploader_id, (SELECT id FROM (SELECT id FROM users WHERE role = 'ADMIN' ORDER BY id LIMIT 1) t))
WHERE status NOT IN ('APPROVED', 'PENDING', 'REJECTED');

-- 通知表
CREATE TABLE IF NOT EXISTS notification (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NOT NULL COMMENT '接收者',
    type        VARCHAR(30)  NOT NULL COMMENT 'UPLOAD/APPROVE/REJECT/USER/COMMENT/SYSTEM',
    message     VARCHAR(500) NOT NULL,
    link        VARCHAR(300) DEFAULT NULL,
    is_read     TINYINT      NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_notif_user (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知';

-- 批注 / 彩色笔记（仅本人可见）
CREATE TABLE IF NOT EXISTS annotation (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    book_id         INT          NOT NULL,
    user_id         INT          NOT NULL,
    chapter_index   INT          NOT NULL,
    paragraph_index INT          NOT NULL COMMENT '段落序号，从 0 开始',
    quote           VARCHAR(500) NOT NULL COMMENT '被标记的原文',
    note            VARCHAR(1000) NOT NULL COMMENT '批注内容',
    color           VARCHAR(20)  NOT NULL DEFAULT '#f5c242',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_anno_book_user (book_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='阅读批注';

-- 章节评论（所有人可见）
CREATE TABLE IF NOT EXISTS comment (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    book_id       INT          NOT NULL,
    chapter_index INT          NOT NULL,
    user_id       INT          NOT NULL,
    content       VARCHAR(1000) NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_comment_book (book_id, chapter_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节评论';

-- 打星评分（每人每书一票）
CREATE TABLE IF NOT EXISTS rating (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    book_id    INT      NOT NULL,
    user_id    INT      NOT NULL,
    stars      INT      NOT NULL COMMENT '1-5',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rating (book_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书评分';

SELECT '权限 / 审核 / 社交功能升级完成' AS message;
