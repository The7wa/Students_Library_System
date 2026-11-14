-- ============================================================
--  墨卷书阁 · 个人资料 / 我的笔记 / 批注互动 升级脚本
--  适用：已经执行过 library_db.sql / upgrade_social.sql 的数据库
--  可重复执行
--  执行：mysql -uroot -p --default-character-set=utf8mb4 < sql\upgrade_profile_notes.sql
-- ============================================================

USE library_db;

-- users：头像、个人简介、界面语言（可重复执行）
SET @add_avatar = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE users ADD COLUMN avatar VARCHAR(16) DEFAULT NULL COMMENT ''头像 emoji'' AFTER email',
        'SELECT ''avatar 已存在，跳过'' AS message')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'avatar'
);
PREPARE stmt_avatar FROM @add_avatar;
EXECUTE stmt_avatar;
DEALLOCATE PREPARE stmt_avatar;

SET @add_bio = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE users ADD COLUMN bio VARCHAR(255) DEFAULT NULL COMMENT ''个人简介'' AFTER avatar',
        'SELECT ''bio 已存在，跳过'' AS message')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'bio'
);
PREPARE stmt_bio FROM @add_bio;
EXECUTE stmt_bio;
DEALLOCATE PREPARE stmt_bio;

SET @add_lang = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE users ADD COLUMN lang VARCHAR(5) NOT NULL DEFAULT ''zh'' COMMENT ''界面语言 zh/en'' AFTER bio',
        'SELECT ''lang 已存在，跳过'' AS message')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'lang'
);
PREPARE stmt_lang FROM @add_lang;
EXECUTE stmt_lang;
DEALLOCATE PREPARE stmt_lang;

-- 批注评论（支持回复）
CREATE TABLE IF NOT EXISTS annotation_comment (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    annotation_id INT          NOT NULL,
    user_id       INT          NOT NULL,
    parent_id     INT          DEFAULT NULL COMMENT '回复的父评论 id',
    content       VARCHAR(1000) NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_annocomment (annotation_id, parent_id, created_at),
    KEY idx_annocomment_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批注评论';

-- 批注点赞
CREATE TABLE IF NOT EXISTS annotation_like (
    annotation_id INT NOT NULL,
    user_id       INT NOT NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (annotation_id, user_id),
    KEY idx_annolike_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批注点赞';

-- 旧数据里同一段内容可能被多次标记：只保留最后一次（id 最大）
DELETE a1 FROM annotation a1
JOIN annotation a2
  ON a1.user_id = a2.user_id
 AND a1.book_id = a2.book_id
 AND a1.chapter_index = a2.chapter_index
 AND a1.paragraph_index = a2.paragraph_index
 AND a1.quote = a2.quote
 AND a1.id < a2.id;

SELECT '个人资料 / 笔记 / 批注互动升级完成' AS message,
       (SELECT COUNT(*) FROM annotation) AS annotations,
       (SELECT COUNT(*) FROM annotation_comment) AS annotation_comments;
