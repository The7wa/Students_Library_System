-- ============================================================
--  墨卷书阁 · 图书管理系统 数据库初始化脚本
--  MySQL 8.0 / utf8mb4
--  注意：脚本会重建 library_db 中的 users、category、book 三张表
--  执行：mysql -uroot -p --default-character-set=utf8mb4 < library_db.sql
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS library_db
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE library_db;

DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录名',
    password    VARCHAR(255) NOT NULL COMMENT 'PBKDF2 哈希',
    nickname    VARCHAR(50)  NOT NULL COMMENT '昵称',
    email       VARCHAR(100) DEFAULT NULL,
    role        VARCHAR(10)  NOT NULL DEFAULT 'USER' COMMENT 'ADMIN / USER',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户';

CREATE TABLE category (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(30)  NOT NULL UNIQUE COMMENT '板块名称',
    icon        VARCHAR(16)  NOT NULL DEFAULT '📚' COMMENT 'emoji 图标',
    description VARCHAR(120) DEFAULT NULL COMMENT '一句话简介',
    color       VARCHAR(20)  NOT NULL DEFAULT 'clay' COMMENT '主题色',
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书板块';

CREATE TABLE book (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT           NOT NULL,
    name        VARCHAR(100)  NOT NULL COMMENT '书名',
    author      VARCHAR(50)   NOT NULL COMMENT '作者',
    isbn        VARCHAR(30)   DEFAULT NULL,
    publisher   VARCHAR(80)   DEFAULT NULL,
    description TEXT,
    content_file VARCHAR(255) DEFAULT NULL COMMENT '正文 TXT 文件名',
    content_name VARCHAR(160) DEFAULT NULL COMMENT '正文原始文件名',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_book_category (category_id),
    KEY idx_book_name (name),
    CONSTRAINT fk_book_category FOREIGN KEY (category_id)
        REFERENCES category (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书';

-- 默认账号：admin / admin123（管理员），demo / demo123（普通用户）
-- 密码使用 PBKDF2-HMAC-SHA256 加盐存储，可在 com.library.util.PasswordUtil 中重新生成
INSERT INTO users (username, password, nickname, email, role) VALUES
('admin', 'pbkdf2_sha256$120000$L4u6A9WbxbVmp159MugHww==$cn6P3Kpa60FrAm4OxvXOHfSC242acn10O4rKfH+obTw=', '馆长', 'admin@library.local', 'ADMIN'),
('demo',  'pbkdf2_sha256$120000$lniBGa++LrQZTUFTkhwF7w==$2891y/jNvG8EAh1v7Tx7uI9h0RiuFg/4z+W0/ggVOyQ=', '小书虫', 'demo@library.local', 'USER');

INSERT INTO category (name, icon, description, color, sort_order) VALUES
('文学小说', '📖', '在故事里遇见另一种人生', 'clay',   1),
('计算机技术', '💻', '代码、系统与工程实践', 'ocean',  2),
('历史人文', '🏛️', '从过去读懂今天', 'amber',  3),
('经济管理', '📊', '商业世界的运行逻辑', 'forest', 4),
('科学科普', '🔬', '保持对世界的好奇', 'teal',   5),
('艺术设计', '🎨', '审美是可以被训练的', 'rose',   6),
('外语学习', '🌍', '打开另一扇语言的窗', 'plum',   7),
('少儿读物', '🧸', '给孩子的第一本好书', 'slate',  8);

INSERT INTO book (category_id, name, author, isbn, publisher, description, create_time) VALUES
-- 文学小说
(1, '活着', '余华', '9787506365437', '作家出版社', '一个人和他命运之间的友情。福贵的一生，是中国人在苦难中活下去的缩影。', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, '百年孤独', '加西亚·马尔克斯', '9787544253994', '南海出版公司', '布恩迪亚家族七代人的传奇，魔幻现实主义文学的巅峰之作。', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(1, '平凡的世界', '路遥', '9787530216781', '北京十月文艺出版社', '在时代洪流里，普通人如何保有尊严与热望。茅盾文学奖经典。', DATE_SUB(NOW(), INTERVAL 16 DAY)),
(1, '围城', '钱钟书', '9787020024759', '人民文学出版社', '婚姻是一座围城，城外的人想进去，城里的人想出来。', DATE_SUB(NOW(), INTERVAL 23 DAY)),
(1, '额尔古纳河右岸', '迟子建', '9787020139590', '人民文学出版社', '一位鄂温克族老人的自述，记录一个民族与森林的百年共生。', DATE_SUB(NOW(), INTERVAL 30 DAY)),
-- 计算机技术
(2, '深入理解计算机系统', '兰德尔·E·布莱恩特', '9787111544937', '机械工业出版社', '从程序员视角理解计算机系统，CSAPP 第 3 版，常读常新。', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'Java核心技术 卷I', '凯·S·霍斯特曼', '9787111636663', '机械工业出版社', 'Java 基础知识的权威参考，覆盖语言特性与核心类库。', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, '算法导论', '托马斯·H·科尔曼', '9787111407010', '机械工业出版社', '算法领域的百科全书，系统讲解设计、分析与证明方法。', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(2, '代码整洁之道', '罗伯特·C·马丁', '9787115216878', '人民邮电出版社', '写出让人愿意读的代码：命名、函数、注释与重构的实践准则。', DATE_SUB(NOW(), INTERVAL 19 DAY)),
(2, 'MySQL必知必会', '本·福达', '9787115191120', '人民邮电出版社', '从 SELECT 到存储过程，一本短小精悍的 MySQL 入门手册。', DATE_SUB(NOW(), INTERVAL 26 DAY)),
-- 历史人文
(3, '人类简史', '尤瓦尔·赫拉利', '9787508647357', '中信出版社', '从认知革命到科学革命，重新讲述智人如何主宰地球。', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, '万历十五年', '黄仁宇', '9787101054491', '中华书局', '以 1587 年为切口，看一个王朝如何在制度惯性中走向衰落。', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(3, '中国历代政治得失', '钱穆', '9787510807213', '九州出版社', '汉唐宋明清五朝政治制度提纲挈领，理解传统中国的钥匙。', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(3, '枪炮、病菌与钢铁', '贾雷德·戴蒙德', '9787508633214', '中信出版社', '为什么是欧亚大陆主导了世界？从地理与生态寻找答案。', DATE_SUB(NOW(), INTERVAL 22 DAY)),
(3, '显微镜下的大明', '马伯庸', '9787540488347', '湖南文艺出版社', '六个真实历史案件，看见明代基层社会的烟火与博弈。', DATE_SUB(NOW(), INTERVAL 29 DAY)),
-- 经济管理
(4, '经济学原理', 'N·格里高利·曼昆', '9787301256909', '北京大学出版社', '十大经济学原理开篇，用生活案例讲透微观与宏观。', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, '穷查理宝典', '彼得·考夫曼', '9787508663442', '中信出版社', '查理·芒格的多元思维模型与人生智慧合集。', DATE_SUB(NOW(), INTERVAL 11 DAY)),
(4, '原则', '瑞·达利欧', '9787508684031', '中信出版社', '把生活和工作变成一套可迭代的系统，桥水创始人的方法论。', DATE_SUB(NOW(), INTERVAL 18 DAY)),
(4, '置身事内', '兰小欢', '9787208171336', '上海人民出版社', '从地方政府投融资讲起，理解中国经济运行的底层逻辑。', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(4, '从0到1', '彼得·蒂尔', '9787508649719', '中信出版社', '创业不是复制，而是创造垄断性的新事物。', DATE_SUB(NOW(), INTERVAL 32 DAY)),
-- 科学科普
(5, '时间简史', '史蒂芬·霍金', '9787535732309', '湖南科学技术出版社', '从大爆炸到黑洞，写给普通人的宇宙学入门。', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(5, '万物简史', '比尔·布莱森', '9787806797884', '接力出版社', '用幽默的笔触串起物理学、生物学与地球科学的发展史。', DATE_SUB(NOW(), INTERVAL 13 DAY)),
(5, '自私的基因', '理查德·道金斯', '9787508694993', '中信出版社', '从基因视角重新理解进化、利他与人类行为。', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(5, '费曼物理学讲义', '理查德·费曼', '9787547844397', '上海科学技术出版社', '物理学家费曼的传奇课堂，保留了对自然最本真的好奇心。', DATE_SUB(NOW(), INTERVAL 27 DAY)),
(5, '宇宙', '卡尔·萨根', '9787543976795', '上海科学技术文献出版社', '把天文、生物与文明史编织成一部壮阔的宇宙史诗。', DATE_SUB(NOW(), INTERVAL 34 DAY)),
-- 艺术设计
(6, '艺术的故事', 'E·H·贡布里希', '9787549407910', '广西美术出版社', '从史前壁画到现代艺术，最受欢迎的艺术史入门经典。', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(6, '设计中的设计', '原研哉', '9787209040723', '山东人民出版社', '无印良品艺术总监谈设计：不是制造，而是唤醒。', DATE_SUB(NOW(), INTERVAL 14 DAY)),
(6, '写给大家看的设计书', '罗宾·威廉姆斯', '9787115430854', '人民邮电出版社', '亲密性、对齐、重复、对比——四原则搞定日常排版。', DATE_SUB(NOW(), INTERVAL 21 DAY)),
(6, '中国美术史', '洪再新', '9787810831222', '中国美术学院出版社', '以作品为线索，梳理中国美术从远古到当代的脉络。', DATE_SUB(NOW(), INTERVAL 28 DAY)),
(6, '色彩设计的原理', '城一夫', '9787508639445', '中信出版社', '从色彩心理到配色方法，建立可复用的色彩判断力。', DATE_SUB(NOW(), INTERVAL 35 DAY)),
-- 外语学习
(7, '新概念英语2', 'L·G·亚历山大', '9787560026855', '外语教学与研究出版社', '经典英语教材第二册，在情景中掌握核心语法与表达。', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(7, '剑桥雅思真题18', '剑桥大学出版社', '9787521342062', '外文出版社', '雅思官方真题，听说读写四科完整训练与解析。', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(7, '英语语法新思维', '张满胜', '9787802563483', '群言出版社', '从思维层面理解语法，而不是死记规则。', DATE_SUB(NOW(), INTERVAL 17 DAY)),
(7, '日语五十音图速记', '安宁', '9787513556774', '外语教学与研究出版社', '联想记忆法攻克五十音，日语零基础的第一本书。', DATE_SUB(NOW(), INTERVAL 24 DAY)),
(7, '翻译的技巧', '钱歌川', '9787559603548', '北京联合出版公司', '老一辈翻译家的经验之谈，中英互译的实用指南。', DATE_SUB(NOW(), INTERVAL 31 DAY)),
-- 少儿读物
(8, '小王子', '安托万·德·圣埃克苏佩里', '9787020042494', '人民文学出版社', '献给所有曾经是孩子的大人，关于爱、责任与告别。', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(8, '夏洛的网', 'E·B·怀特', '9787532733415', '上海译文出版社', '一只蜘蛛用网上的字救了一头小猪，关于友谊与承诺。', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(8, '窗边的小豆豆', '黑柳彻子', '9787544250580', '南海出版公司', '巴学园里的童年故事，让每个孩子被温柔地看见。', DATE_SUB(NOW(), INTERVAL 17 DAY)),
(8, '哈利·波特与魔法石', 'J·K·罗琳', '9787020033430', '人民文学出版社', '霍格沃茨的入学通知书，开启一代人的魔法记忆。', DATE_SUB(NOW(), INTERVAL 24 DAY)),
(8, '昆虫记', '让-亨利·法布尔', '9787107312342', '人民教育出版社', '用文学笔法记录昆虫世界，科学观察与诗意想象的结合。', DATE_SUB(NOW(), INTERVAL 31 DAY));

SET FOREIGN_KEY_CHECKS = 1;

-- 创建项目专用账号：library_app / 123456
-- 只授予 library_db 的权限，项目配置里就不用写 root 密码，上传 GitHub 也更安全。
-- 如果你的 MySQL 不允许 CREATE USER，可以删掉这三行，直接把 db.properties 改成自己的账号。
CREATE USER IF NOT EXISTS 'library_app'@'localhost' IDENTIFIED BY '123456';
ALTER USER 'library_app'@'localhost' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON library_db.* TO 'library_app'@'localhost';
FLUSH PRIVILEGES;

SELECT '数据库初始化完成' AS message,
       (SELECT COUNT(*) FROM category) AS categories,
       (SELECT COUNT(*) FROM book) AS books,
       (SELECT COUNT(*) FROM users) AS users;
