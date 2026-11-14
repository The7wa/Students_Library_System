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

DROP TABLE IF EXISTS annotation_comment;
DROP TABLE IF EXISTS annotation_like;
DROP TABLE IF EXISTS paragraph_comment;
DROP TABLE IF EXISTS rating;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS annotation;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录名',
    password    VARCHAR(255) NOT NULL COMMENT 'PBKDF2 哈希',
    nickname    VARCHAR(50)  NOT NULL COMMENT '昵称',
    email       VARCHAR(100) DEFAULT NULL,
    avatar      VARCHAR(16)  DEFAULT NULL COMMENT '头像 emoji',
    bio         VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
    lang        VARCHAR(5)   NOT NULL DEFAULT 'zh' COMMENT '界面语言 zh/en',
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
    uploader_id  INT           DEFAULT NULL COMMENT '上传者',
    status       VARCHAR(20)   NOT NULL DEFAULT 'APPROVED' COMMENT 'APPROVED / PENDING / REJECTED',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_book_category (category_id),
    KEY idx_book_name (name),
    KEY idx_book_status (status),
    CONSTRAINT fk_book_category FOREIGN KEY (category_id)
        REFERENCES category (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书';

CREATE TABLE notification (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NOT NULL COMMENT '接收者',
    type        VARCHAR(30)  NOT NULL COMMENT 'UPLOAD/APPROVE/REJECT/USER/COMMENT/SYSTEM',
    message     VARCHAR(500) NOT NULL,
    link        VARCHAR(300) DEFAULT NULL,
    is_read     TINYINT      NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_notif_user (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知';

CREATE TABLE annotation (
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

CREATE TABLE annotation_comment (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    annotation_id INT          NOT NULL,
    user_id       INT          NOT NULL,
    parent_id     INT          DEFAULT NULL COMMENT '回复的父评论 id',
    content       VARCHAR(1000) NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_annocomment (annotation_id, parent_id, created_at),
    KEY idx_annocomment_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批注评论';

CREATE TABLE annotation_like (
    annotation_id INT NOT NULL,
    user_id       INT NOT NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (annotation_id, user_id),
    KEY idx_annolike_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批注点赞';

CREATE TABLE paragraph_comment (
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

CREATE TABLE comment (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    book_id       INT          NOT NULL,
    chapter_index INT          NOT NULL,
    user_id       INT          NOT NULL,
    content       VARCHAR(1000) NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_comment_book (book_id, chapter_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节评论';

CREATE TABLE rating (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    book_id    INT      NOT NULL,
    user_id    INT      NOT NULL,
    stars      INT      NOT NULL COMMENT '1-5',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rating (book_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书评分';

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
(1, '重生之苦难千千万，我偏要活着', '余华', '9787506365437', '番茄星际出版社', '重生那天我发现自己又躺回那间破屋，苦难排着队来敲门，可我偏要活着，还要活得比谁都响亮。', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, '重生七代，我家的诅咒是孤独', '加西亚·马尔克斯', '9787544253994', '马尔克斯诅咒出版局', '我家七代人共用同一个名字、同一场孤独，第七代我决定把诅咒写进遗嘱里卖掉。', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(1, '我在黄土坡种地，顺便惊艳了整个双水村', '路遥', '9787530216781', '黄土坡文艺社', '我在双水村种地，顺手把整村人的命运重排了一遍，连老天都来问我下一季种什么。', DATE_SUB(NOW(), INTERVAL 16 DAY)),
(1, '闪婚教授后，全城都说我进了围城', '钱钟书', '9787020024759', '围城婚姻事务所', '闪婚一位教授后，全城都说我掉进围城；我反手在城墙上开了个门，收费参观。', DATE_SUB(NOW(), INTERVAL 23 DAY)),
(1, '额尔古纳河右岸：我的驯鹿成精了', '迟子建', '9787020139590', '驯鹿出版社', '额尔古纳河右岸的驯鹿忽然成精，开口第一句就是：这个民族的故事，我来给你讲。', DATE_SUB(NOW(), INTERVAL 30 DAY)),
-- 计算机技术
(2, '重生之我从晶体管卷到操作系统之神', '兰德尔·E·布莱恩特', '9787111544937', '硅谷卷王出版社', '重生回大一，我从晶体管一路卷到操作系统内核，毕业时整个硅谷排队给我递简历。', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, '我在Java帝国当技术万一百年', '凯·S·霍斯特曼', '9787111636663', '爪哇帝国印书馆', '我穿进Java帝国当技术万一百年，代码即律法，一次GC风暴让全世界记住了我的名字。', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, '刷完这本算法，我吊打了整个硅谷', '托马斯·H·科尔曼', '9787111407010', '秃头算法研究院', '刷完这本算法，我用一段O(1)解法吊打整个硅谷，面试官跪着求我别再优化了。', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(2, '代码整洁之道：我把屎山重构成GitHub第一', '罗伯特·C·马丁', '9787115216878', '重构教父出版社', '我把公司那座三百万行屎山重构成GitHub第一，顺手让所有前任作者集体注销账号。', DATE_SUB(NOW(), INTERVAL 19 DAY)),
(2, '我的MySQL从入门到删库跑路', '本·福达', '9787115191120', '删库跑路书局', '我的MySQL从入门到精通再到删库跑路，凌晨三点的一条DROP，让整个机房为我屏息。', DATE_SUB(NOW(), INTERVAL 26 DAY)),
-- 历史人文
(3, '开局一个智人，我进化成了万物之王', '尤瓦尔·赫拉利', '9787508647357', '智人进化出版署', '开局一个智人，我靠讲故事统一部落，一路进化成万物之王，连猛犸象都给我让路。', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, '重生万历十五年，我只想躺平当皇帝', '黄仁宇', '9787101054491', '万历躺平出版局', '重生万历十五年，我只想躺平当皇帝，结果满朝文武跪着求我别躺，说大明需要我。', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(3, '穿越历代朝堂，我把皇帝说得哑口无言', '钱穆', '9787510807213', '朝堂舌战丛书社', '穿越历代朝堂，我把每位皇帝都说得哑口无言，最后他们集体请我当言官。', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(3, '开局大陆一块，我靠枪炮病菌统一全球', '贾雷德·戴蒙德', '9787508633214', '枪炮病菌出版集团', '开局只有一块大陆，我靠枪炮、病菌与钢铁三件套，统一全球还顺手写了本畅销书。', DATE_SUB(NOW(), INTERVAL 22 DAY)),
(3, '我在大明当县吏，掀翻了六个部', '马伯庸', '9787540488347', '大明县衙出版社', '我在大明当县吏，靠六桩小案掀翻六个部，京城里从此流传我的名字。', DATE_SUB(NOW(), INTERVAL 29 DAY)),
-- 经济管理
(4, '重生之我用一只看不见的手拿捏全世界', 'N·格里高利·曼昆', '9787301256909', '看不见的手书局', '重生之我握住那只看不见的手，随便一调利率，全世界物价跟着我呼吸。', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, '穷查理宝典：和巴菲特搭档三十年后我悟了', '彼得·考夫曼', '9787508663442', '伯克希尔智慧社', '穷查理宝典：和巴菲特搭档三十年后我悟了——原来复利也怕我。', DATE_SUB(NOW(), INTERVAL 11 DAY)),
(4, '我的原则：先赢一次再说', '瑞·达利欧', '9787508684031', '达利欧原则文库', '我的原则只有一条：先赢一次再说。剩下的九十九条都是给它注释。', DATE_SUB(NOW(), INTERVAL 18 DAY)),
(4, '置身事内：我在衙门里看懂了中国经济', '兰小欢', '9787208171336', '地方财政观察社', '置身事内：我在衙门里看懂了中国经济，还顺手把一张资产负债表背了下来。', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(4, '从0到1：我投资的项目全成了独角兽', '彼得·蒂尔', '9787508649719', '独角兽创投社', '从0到1：我投的项目全成了独角兽，后来它们排队想收购我。', DATE_SUB(NOW(), INTERVAL 32 DAY)),
-- 科学科普
(5, '重生之我把时间当人质', '史蒂芬·霍金', '9787535732309', '霍金时间编辑部', '重生之我把时间当人质，黑洞在我办公室门口等了三个小时。', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(5, '万物简史：从奇点到人类，全程我在场', '比尔·布莱森', '9787806797884', '万物在场出版公司', '万物简史：从奇点到人类，全程我在场，还顺便给宇宙拍了一张全家福。', DATE_SUB(NOW(), INTERVAL 13 DAY)),
(5, '我的基因有点自私，它想统治地球', '理查德·道金斯', '9787508694993', '基因帝国出版社', '我的基因有点自私，它想统治地球；我只能连夜和它签了一份停战协议。', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(5, '物理之神开课实录：我讲物理无人敢逃课', '理查德·费曼', '9787547844397', '费曼课堂实录社', '物理之神开课实录：我讲物理无人敢逃课，连光速都提前一分钟到教室。', DATE_SUB(NOW(), INTERVAL 27 DAY)),
(5, '开局一个奇点，我爆出了整个宇宙', '卡尔·萨根', '9787543976795', '萨根宇宙书局', '开局一个奇点，我一跺脚爆出了整个宇宙，星系排着队问我要签名。', DATE_SUB(NOW(), INTERVAL 34 DAY)),
-- 艺术设计
(6, '重生文艺复兴，我承包了整个艺术史', 'E·H·贡布里希', '9787549407910', '贡布里希艺术社', '重生文艺复兴，我承包了整个艺术史，达芬奇来给我磨颜料。', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(6, '设计之神：我随手一张稿，甲方跪着喊绝', '原研哉', '9787209040723', '原研哉设计局', '设计之神：我随手一张稿，甲方跪着喊绝，从此再没人敢说改回第一版。', DATE_SUB(NOW(), INTERVAL 14 DAY)),
(6, '全网设计师都在抄我的作业', '罗宾·威廉姆斯', '9787115430854', '四原则设计社', '全网设计师都在抄我的作业，我干脆开了个抄作业付费通道。', DATE_SUB(NOW(), INTERVAL 21 DAY)),
(6, '我在千年画坛当卷王，从敦煌卷到紫禁城', '洪再新', '9787810831222', '千年画坛卷王社', '我在千年画坛当卷王，从敦煌卷到紫禁城，最后连皇帝都来求我别画了。', DATE_SUB(NOW(), INTERVAL 28 DAY)),
(6, '我靠配色拿捏了全人类的眼睛', '城一夫', '9787508639445', '色彩掌控出版局', '我靠配色拿捏了全人类的眼睛，看一眼就再也忘不掉那种红。', DATE_SUB(NOW(), INTERVAL 35 DAY)),
-- 外语学习
(7, '背完新概念，我在牛津当考神', 'L·G·亚历山大', '9787560026855', '牛津考神书局', '背完新概念，我在牛津当考神，考官当场把证书改成我的名字。', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(7, '雅思18套真题刷穿，考官当场跪了', '剑桥大学出版社', '9787521342062', '雅思跪了出版公司', '雅思18套真题刷穿，四科满分，考官当场跪了并问我能不能来出题。', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(7, '语法之神的自我修养', '张满胜', '9787802563483', '语法神殿出版社', '语法之神的自我修养：我一句话让所有从句归位，时态集体敬礼。', DATE_SUB(NOW(), INTERVAL 17 DAY)),
(7, '三天背完五十音，我在东京横着走', '安宁', '9787513556774', '东京横行书店', '三天背完五十音，我在东京横着走，连自动贩卖机都主动给我找零。', DATE_SUB(NOW(), INTERVAL 24 DAY)),
(7, '重生之我同传箱里装着巴别塔', '钱歌川', '9787559603548', '巴别塔同传社', '重生之我同传箱里装着巴别塔，一场会议下来，五种语言同时为我鼓掌。', DATE_SUB(NOW(), INTERVAL 31 DAY)),
-- 少儿读物
(8, 'B-612星球小王子：我驯养了全宇宙的玫瑰', '安托万·德·圣埃克苏佩里', '9787020042494', 'B-612星球出版社', 'B-612星球小王子：我驯养了全宇宙的玫瑰，狐狸排着队等我签名。', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(8, '我在猪圈织网，救下了全网最红的猪', 'E·B·怀特', '9787532733415', '谷仓织网出版局', '我在猪圈织网，用四个字救下那头猪，它后来成了全网最红的顶流。', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(8, '被退学后，我成了全校最快乐的孩子', '黑柳彻子', '9787544250580', '巴学园童书社', '被退学后，我成了全校最快乐的孩子，校长说这所学校就是为我建的。', DATE_SUB(NOW(), INTERVAL 17 DAY)),
(8, '重生之我在霍格沃茨抽到SSR魔法石', 'J·K·罗琳', '9787020033430', '霍格沃茨魔法社', '重生之我在霍格沃茨抽到SSR魔法石，伏地魔看到我的抽卡记录直接删号。', DATE_SUB(NOW(), INTERVAL 24 DAY)),
(8, '我的昆虫帝国：蚂蚁向我俯首称臣', '让-亨利·法布尔', '9787107312342', '昆虫帝国出版社', '我的昆虫帝国：蚂蚁向我俯首称臣，螳螂做了我的侍卫长，蝉负责报时。', DATE_SUB(NOW(), INTERVAL 31 DAY));

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
