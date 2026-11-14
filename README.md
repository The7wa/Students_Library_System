# 墨卷书阁 · 图书管理系统

一个用 **JSP + Servlet + JDBC + MySQL** 实现的图书管理系统，按 **图书板块 → 书架 → 图书** 三层组织藏书。

当前版本包含：注册登录、角色权限、图书/板块管理、TXT 在线阅读、阅读进度、私有批注、段落公开评论/回复、星级评分、站内通知、个人主页、设置、我的笔记、中英文界面与阅读主题。

> 项目使用 `javax.servlet`，对应 **Tomcat 9**。Tomcat 10+ 是 `jakarta.servlet`，会报 `ClassNotFoundException`。

---

## 功能

### 账户与个人中心

- 点击右上角头像打开菜单：**个人主页 / 我的笔记 / 设置 / 退出登录**
- 个人主页：查看上传数、批注数、评论数、评分记录；修改昵称、邮箱、个人简介和 emoji 头像
- 设置：
  - 修改密码
  - 注销账户（普通用户可用；管理员账号受保护）
  - 切换中文 / English 界面
  - 切换纸墨 / 青竹 / 夜读三种主题
- 我的笔记：汇总自己在所有书中划过的重点、颜色和文字笔记；同一段原文只显示最后一次颜色和笔记

### 阅读与互动

- 上传 TXT 正文后分章阅读
- 划选正文添加彩色批注，同一段原文重复标记只更新最后一条，不会变成日志列表
- 「我的批注」只显示自己的笔记，笔记内容仅本人可见，个人批注不会公开
- 段落旁显示公开评论数徽标，点击打开评论弹窗：
  - 所有登录用户查看评论
  - 发表评论或回复（一层回复）
  - 评论和回复所有人可见
- 章末「本章评论」、图书星级评分、平均分展示
- 阅读进度保存在浏览器本地

### 权限与审核

| 功能 | 管理员 | 普通用户 |
| --- | --- | --- |
| 用户管理 | ✅ | ❌ |
| 板块管理 | ✅ | ❌ |
| 图书管理 | ✅ | 仅上传、删除自己上传的 |
| 审核读者上传 | ✅ | ❌ |
| 删除任意评论 | ✅ | 只能删自己的 |
| 阅读、批注、评论、打分 | ✅ | ✅ |

- 普通用户上传图书后为「待审核」，管理员通过后才上架
- 管理员上传直接上架；`admin` 为最高管理员
- 站内通知：新用户注册、上传待审核、审核通过/驳回、评论等

---

## 技术栈

- Java 17（JDK 17/21/25 均可）
- JSP 2.3 / Servlet 4.0（`javax.servlet`）
- JSTL 1.2
- MySQL 8.0 + mysql-connector-java 8.0.30
- 原生 JDBC + DAO 分层，不依赖 Spring / Maven
- IntelliJ IDEA 传统 Web 工程（WebContent + exploded war）

---

## 在 IntelliJ IDEA 中运行

1. 用 IDEA 打开本目录（不是它的上一级目录）
2. `Project SDK` 选择 JDK 17 或以上
3. 确认 `Settings → Build, Execution, Deployment → Application Servers` 中有 **Tomcat 9.0.30**
4. 右上角运行配置选择 **Tomcat 9.0.30**，启动
5. 访问：**http://localhost:8080/library/**

IDEA 本地工程已配置：

- Web 根目录：`WebContent`
- Artifact：`jspsweb:Web exploded`
- 部署上下文：`/library`
- 编译级别：Java 17

> `.idea/workspace.xml` 含本机路径，默认不提交 Git。换机器后如果没有运行配置：
> `Run → Edit Configurations → + → Tomcat Server → Local`，
> `Deployment → + → Artifact → jspsweb:Web exploded`，
> `Application context` 填 `/library`。

### 默认账号

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `admin123` | 管理员 |
| `demo` | `demo123` | 普通用户 |

---

## 数据库

默认使用专用账号：

| 配置 | 值 |
| --- | --- |
| 数据库 | `library_db` |
| 用户名 | `library_app` |
| 密码 | `123456` |

配置：[src/db.properties](src/db.properties)

### 首次建库（会重建表）

```bat
mysql -uroot -p --default-character-set=utf8mb4 < sql\library_db.sql
```

### 已有旧库升级（保留数据）

按顺序执行：

```bat
mysql -uroot -p --default-character-set=utf8mb4 < sql\upgrade_reading.sql
mysql -uroot -p --default-character-set=utf8mb4 < sql\upgrade_social.sql
mysql -uroot -p --default-character-set=utf8mb4 < sql\upgrade_profile_notes.sql
mysql -uroot -p --default-character-set=utf8mb4 < sql\upgrade_paragraph_comments.sql
```

`upgrade_profile_notes.sql` 会新增：

- `users.avatar`、`users.bio`、`users.lang`
- 清理同一段原文的重复批注，只保留最后一条

`upgrade_paragraph_comments.sql` 会新增：

- `paragraph_comment`：段落公开评论 / 回复（与私有批注分离）

---

## 目录结构

```text
Students_Library_System/
├── src/
│   ├── db.properties
│   └── com/library/
│       ├── entity/        User / Book / Category / Annotation / AnnotationComment ...
│       ├── dao/           User / Book / Annotation / AnnotationComment / Rating ...
│       ├── servlet/       Auth / Home / Book / Read / User / Annotation / Admin ...
│       ├── filter/        Encoding / Auth / Notification
│       └── util/          DBUtil / PasswordUtil / TXT 解析 / 搜索高亮
├── WebContent/
│   ├── login.jsp / register.jsp
│   ├── assets/            CSS / JS / i18n.js
│   ├── WEB-INF/web.xml
│   ├── WEB-INF/views/     home / shelf / reader / profile / settings / my-notes ...
│   └── WEB-INF/lib/       JSTL + MySQL 驱动
├── lib/servlet-api.jar   仅编译用
├── sql/                  全量建库 + 旧库升级脚本
├── .idea/                IDEA 工程配置
└── jspsweb.iml
```

---

## 常见问题

### 访问 `http://localhost:8080/` 是 404

正常。项目部署在 `/library`，请访问：

```text
http://localhost:8080/library/
```

### 登录后 500，提示 Unknown column / Table doesn't exist

数据库不是最新结构，执行：

```bat
mysql -uroot -p --default-character-set=utf8mb4 < sql\upgrade_reading.sql
mysql -uroot -p --default-character-set=utf8mb4 < sql\upgrade_social.sql
mysql -uroot -p --default-character-set=utf8mb4 < sql\upgrade_profile_notes.sql
```

### 英文界面

在「设置 → 界面语言」选择 English。英文模式翻译导航、按钮、账户页面和阅读互动文案；图书名、批注/评论原文等内容保持原文。

### TXT 正文上传目录

默认保存到：

```text
C:\Users\你的用户名\library-books
```

如果 Tomcat 没有写权限，可在 Tomcat VM options 加：

```text
-Dlibrary.book.dir=D:\library-books
```

---

## 说明

这是课程设计项目，未接入连接池，适合本地演示与学习使用。
