<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>登录 · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css">
</head>
<body class="auth-page">
<div class="auth-shell">
    <aside class="auth-aside">
        <div class="auth-aside-top">
            <a class="brand" href="${ctx}/login.jsp">
                <span class="brand-mark">墨</span>
                <span class="brand-text">
                    <strong>墨卷书阁</strong>
                    <small>LIBRARY SYSTEM</small>
                </span>
            </a>
        </div>

        <div class="auth-aside-copy">
            <span class="eyebrow">A QUIET PLACE FOR BOOKS</span>
            <h1>把每一本书，<br>放在它<em>该在</em>的位置。</h1>
            <p>墨卷书阁是一间属于你的数字书房。用板块整理藏书，用书架安放阅读，让找书这件事重新变得从容。</p>
            <div class="auth-points">
                <span><i></i> 八大图书板块</span>
                <span><i></i> 全馆快速检索</span>
                <span><i></i> 藏书增删改查</span>
            </div>
        </div>

        <div class="auth-aside-bottom">
            <div class="book-stack" aria-hidden="true">
                <span style="--h:72px;--c:#b4552d;--r:-4deg"></span>
                <span style="--h:95px;--c:#3f6b53"></span>
                <span style="--h:64px;--c:#c99a4b;--r:3deg"></span>
                <span style="--h:84px;--c:#6b4a6e;--r:-2deg"></span>
                <span style="--h:58px;--c:#2f5d7c;--r:5deg"></span>
            </div>
            <span>JSP · SERVLET · MYSQL</span>
        </div>
    </aside>

    <main class="auth-main">
        <div class="auth-card">
            <span class="auth-kicker">WELCOME BACK</span>
            <h2>欢迎回来</h2>
            <p class="auth-sub">登录后继续整理你的书阁</p>

            <c:if test="${param.logout eq '1'}">
                <div class="auth-alert info">你已安全退出登录，期待下次再见。</div>
            </c:if>

            <c:if test="${not empty errors.login}">
                <div class="auth-alert">
                    <span>!</span>
                    <span><c:out value="${errors.login}"/></span>
                </div>
            </c:if>

            <form class="auth-form" action="${ctx}/auth" method="post" novalidate>
                <input type="hidden" name="action" value="login">

                <div class="field">
                    <label for="username">用户名</label>
                    <input class="input ${not empty errors.username ? 'error' : ''}"
                           id="username" name="username" type="text"
                           value="${fn:escapeXml(username)}"
                           placeholder="请输入用户名" autocomplete="username" autofocus>
                    <c:if test="${not empty errors.username}">
                        <span class="error-text"><c:out value="${errors.username}"/></span>
                    </c:if>
                </div>

                <div class="field">
                    <label for="password">密码</label>
                    <div class="password-wrap">
                        <input class="input ${not empty errors.password ? 'error' : ''}"
                               id="password" name="password" type="password"
                               placeholder="请输入密码" autocomplete="current-password">
                        <button type="button" class="toggle-password" data-toggle-password="password">显示</button>
                    </div>
                    <c:if test="${not empty errors.password}">
                        <span class="error-text"><c:out value="${errors.password}"/></span>
                    </c:if>
                </div>

                <button type="submit" class="btn btn-primary auth-submit">进入书阁 →</button>
            </form>

            <p class="auth-switch">还没有账号？<a href="${ctx}/register.jsp">注册一个</a></p>

            <p class="auth-mini-footer">© 2026 墨卷书阁 · 图书管理系统课程设计</p>
        </div>
    </main>
</div>
<script src="${ctx}/assets/js/app.js"></script>
</body>
</html>
