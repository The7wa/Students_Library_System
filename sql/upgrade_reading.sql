-- ============================================================
--  墨卷书阁 · 在线阅读功能升级脚本（已有数据库使用）
--  适用：已经执行过 library_db.sql，不想重建数据的同学
--  执行：mysql -uroot -p --default-character-set=utf8mb4 < sql/upgrade_reading.sql
-- ============================================================

USE library_db;

-- book 表新增正文文件字段（可重复执行）
SET @add_content_file = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE book ADD COLUMN content_file VARCHAR(255) DEFAULT NULL COMMENT ''正文 TXT 文件名'' AFTER description',
        'SELECT ''content_file 已存在，跳过'' AS message'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'book'
      AND COLUMN_NAME = 'content_file'
);
PREPARE stmt_content_file FROM @add_content_file;
EXECUTE stmt_content_file;
DEALLOCATE PREPARE stmt_content_file;

SET @add_content_name = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE book ADD COLUMN content_name VARCHAR(160) DEFAULT NULL COMMENT ''正文原始文件名'' AFTER content_file',
        'SELECT ''content_name 已存在，跳过'' AS message'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'book'
      AND COLUMN_NAME = 'content_name'
);
PREPARE stmt_content_name FROM @add_content_name;
EXECUTE stmt_content_name;
DEALLOCATE PREPARE stmt_content_name;

-- 说明：旧库里的 price、stock 列可以保留，历史数据不会丢；
-- 新代码和页面已经不再使用价格与库存。如果确定不再需要，可以手动执行：
-- ALTER TABLE book DROP COLUMN price, DROP COLUMN stock;

SELECT '在线阅读功能字段已就绪' AS message,
       (SELECT COUNT(*) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'book'
          AND COLUMN_NAME IN ('content_file', 'content_name')) AS content_columns;
