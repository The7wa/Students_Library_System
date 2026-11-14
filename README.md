# 墨卷书阁 · 图书管理系统（进阶功能版）

一个用 **JSP + Servlet + JDBC + MySQL** 实现的图书管理系统，按 **图书板块 → 书架 → 图书** 三层组织藏书。

在基础增删改查之上，当前版本增加了：角色权限、上传审核、站内通知、TXT 在线阅读、阅读进度、批注、章节评论、星级评分。

> 本项目使用 `javax.servlet`，对应 **Tomcat 9**。Tomcat 10+ 是 `jakarta.servlet`，会报 `ClassNotFoundException`。

---

## 功能

### 角色与权限

| 功能 | 管理员 | 普通用户 |
| --- | --- | --- |
| 用户管理（增 / 修 / 删） | ✅ | ❌ |
| 板块管理（增 / 修 / 删） | ✅ | ❌ |
| 图书管理（增 / 修 / 删） | ✅ | 仅上传、删除自己上传的 |
| 审核读者上传 | ✅ | ❌ |
| 删除任意评论 | ✅ | 只能删自己的 |
| 阅读、批注、评论、打分 | ✅ | ✅ |

- 普通用户上传图书后状态为「待审核」，管理员通过后才出现在书架。
- 管理员上传的图书直接上架。
- 普通用户只能删除自己上传的图书。
- `admin` 是系统最高管理员；其他管理员不能删除、降级或修改 `admin` 账号。

### 通知

- 管理员收到：新用户注册、读者上传待审核、读者发表评论、读者删除自己的上传。
- 普通用户收到：自己上传作品的审核通过 / 驳回（含驳回原因）。
- 顶部导航「通知」有未读角标，支持全部标记已读。

### 阅读与互动

- 上传 TXT 正文后，可在网页内分章阅读。
- 右侧「我的批注」栏：显示当前章节的批注，点击可定位到原文并短暂高亮。
- 鼠标划选正文文字 → 弹出批注框，可选 5 种颜色，填写笔记；**批注只有本人可见**。
- 每章底部「本章评论」：所有登录用户可发表、查看；本人可删自己的，管理员可删任何人的。
- 图书详情页可打 1-5 星，每人每书一票、可修改；展示平均分和评分人数。
- 书架 / 搜索卡片上显示平均分。
- 阅读进度保存在浏览器本地，下次打开继续读。

---

## 技术栈

- Java 17（JDK 17/21/25 均可）
- JSP 2.3 / Servlet 4.0（`javax.servlet`）
- JSTL 1.2
- MySQL 8.0 + mysql-connector-java 8.0.30
- 原生 JDBC + DAO 分层，不依赖 Spring / Maven
- IntelliJ IDEA 传统 Web 工程（WebContent + exploded war）

---

## 目录结构

```text
Students_Library_System/
├── src/
│   ├── db.properties              # 数据库连接配置
│   └── com/library/
│       ├── entity/                # User / Category / Book / Chapter / Comment ...
│       ├── dao/                   # 用户、图书、板块、批注、评论、评分、通知
│       ├── servlet/               # 登录、首页、书架、图书、阅读、管理、通知...
│       ├── filter/                # 编码、登录拦截、通知角标
│       └── util/                  # DBUtil / PasswordUtil / TXT 解析 / 搜索高亮
├── WebContent/
│   ├── login.jsp / register.jsp
│   ├── assets/                    # CSS / JS
│   ├── WEB-INF/web.xml            # Servlet / Filter 注册
│   ├── WEB-INF/views/             # 登录后的页面
│   └── WEB-INF/lib/               # JSTL + MySQL 驱动
├── lib/servlet-api.jar            # 仅编译用，不部署
├── sql/library_db.sql             # 全量建库脚本
├── sql/upgrade_reading.sql        # 旧库补正文阅读字段
├── sql/upgrade_social.sql         # 旧库补审核 / 通知 / 批注 / 评论 / 评分
├── docs/screenshots/              # 页面截图
├── .idea/                         # IDEA 工程配置
└── jspsweb.iml
```

---

## 数据库

项目默认使用专用账号，避免把 root 密码写进项目：

| 配置 | 值 |
| --- | --- |
| 数据库 | `library_db` |
| 用户名 | `library_app` |
| 密码 | `123456` |

配置文件：[src/db.properties](src/db.properties)

### 首次建库

用 root 执行（会重建表）：

```bat
mysql -uroot -p --default-character-set=utf8mb4 < sql\library_db.sql
```

脚本会创建 8 个板块、40 本示例图书、2 个演示账号，并创建 `library_app` 账号。

### 已有旧库升级（保留数据）

```bat
mysql -uroot -p --default-character-set=utf8mb4 < sql\upgrade_reading.sql
mysql -uroot -p --default-character-set=utf8mb4 < sql\upgrade_social.sql
```

> PowerShell 不支持 `<` 重定向时，可以改用 MySQL Workbench 执行，或使用 `cmd /c "mysql ... < sql\..."`。

---

## 在 IntelliJ IDEA 中运行

1. 用 IDEA 打开本目录（不是它的上一级目录）。
2. `Project SDK` 选择 JDK 17 或以上。
3. 确认 `Settings → Build, Execution, Deployment → Application Servers` 里有 **Tomcat 9.0.30**。
4. 右上角运行配置选择 **Tomcat 9.0.30**，点击运行。
5. 浏览器访问：**http://localhost:8080/library/**

本地工程已经配置好：

- Web 根目录：`WebContent`
- Artifact：`jspsweb:Web exploded`
- 部署上下文：`/library`
- 编译级别：Java 17

> `.idea/workspace.xml` 含本机路径，默认不提交到 Git。如果换机器后没有运行配置：
> `Run → Edit Configurations → + → Tomcat Server → Local`，
> `Deployment → + → Artifact → jspsweb:Web exploded`，
> `Application context` 填 `/library`。

### 默认账号

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `admin123` | 管理员 |
| `demo` | `demo123` | 普通用户 |

也可以直接在注册页创建新账号。

---

## 常见问题

### 访问 `http://localhost:8080/` 是 404

正常。项目部署在 `/library`，请访问：

```text
http://localhost:8080/library/
```

### 登录后 500，提示 Unknown column / Table doesn't exist

数据库还是旧结构，执行：

```bat
mysql -uroot -p --default-character-set=utf8mb4 < sql\upgrade_reading.sql
mysql -uroot -p --default-character-set=utf8mb4 < sql\upgrade_social.sql
```

### 中文乱码

- 数据库使用 `utf8mb4`
- JSP 使用 `pageEncoding=UTF-8`
- `EncodingFilter` 统一请求 / 响应编码

IDEA 可设置 `Settings → Editor → File Encodings` 全部为 UTF-8。

### TXT 正文上传目录

默认保存在：

```text
C:\Users\你的用户名\library-books
```

如果 Tomcat 没有写权限，可在 Tomcat 启动配置的 VM options 加：

```text
-Dlibrary.book.dir=D:\library-books
```

---

## 说明

这是课程设计项目，未接入连接池，适合本地演示与学习使用。
