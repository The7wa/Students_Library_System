<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>注册 · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css?v=19">
</head>
<body class="auth-page"><script>(function(){try{var t=localStorage.getItem("siteTheme")||localStorage.getItem("readerTheme");if(t==="green"||t==="black"){document.body.className+=(document.body.className?" ":"")+"theme-"+t;}}catch(e){}})();</script>
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
            <span class="eyebrow">START YOUR LIBRARY</span>
            <h1>从一本书开始，<br>建一座<em>自己的</em>书阁。</h1>
            <p>注册后即可创建板块、上架图书、上传正文。你的每一次录入，都会成为书架上一本可被找到、可被阅读的书。</p>
            <div class="auth-points">
                <span><i></i> 免费注册</span>
                <span><i></i> 数据本地保存</span>
                <span><i></i> 随时管理藏书</span>
            </div>
        </div>

        <div class="auth-aside-bottom">
            <div class="book-stack" aria-hidden="true">
                <span style="--h:64px;--c:#2f5d7c;--r:4deg"></span>
                <span style="--h:88px;--c:#b4552d"></span>
                <span style="--h:58px;--c:#3f6b53;--r:-3deg"></span>
                <span style="--h:96px;--c:#c99a4b;--r:2deg"></span>
                <span style="--h:70px;--c:#a8505f;--r:-5deg"></span>
            </div>
            <span>JSP · SERVLET · MYSQL</span>
        </div>
    </aside>

    <main class="auth-main">
        <div class="auth-card">
            <span class="auth-kicker">CREATE ACCOUNT</span>
            <h2>注册账号</h2>
            <p class="auth-sub">只需一分钟，开启你的数字书房</p>

            <c:if test="${not empty errors}">
                <div class="auth-alert">
                    <span>!</span>
                    <span>请检查标红的字段后重新提交。</span>
                </div>
            </c:if>

            <form class="auth-form" action="${ctx}/auth" method="post" novalidate>
                <input type="hidden" name="action" value="register">

                <div class="field">
                    <label for="username">用户名 <span class="hint">3-20 位字母、数字或下划线</span></label>
                    <input class="input ${not empty errors.username ? 'error' : ''}"
                           id="username" name="username" type="text"
                           value="${fn:escapeXml(username)}"
                           placeholder="例如：bookworm" autocomplete="username" autofocus>
                    <c:if test="${not empty errors.username}">
                        <span class="error-text"><c:out value="${errors.username}"/></span>
                    </c:if>
                </div>

                <div class="field">
                    <label for="nickname">昵称 <span class="hint">显示在右上角</span></label>
                    <input class="input ${not empty errors.nickname ? 'error' : ''}"
                           id="nickname" name="nickname" type="text"
                           value="${fn:escapeXml(nickname)}"
                           placeholder="例如：小书虫" autocomplete="nickname">
                    <c:if test="${not empty errors.nickname}">
                        <span class="error-text"><c:out value="${errors.nickname}"/></span>
                    </c:if>
                </div>

                <div class="field">
                    <label for="email">邮箱 <span class="hint">选填</span></label>
                    <input class="input ${not empty errors.email ? 'error' : ''}"
                           id="email" name="email" type="email"
                           value="${fn:escapeXml(email)}"
                           placeholder="you@example.com" autocomplete="email">
                    <c:if test="${not empty errors.email}">
                        <span class="error-text"><c:out value="${errors.email}"/></span>
                    </c:if>
                </div>

                <div class="field">
                    <label for="password">密码 <span class="hint">6-32 位</span></label>
                    <div class="password-wrap">
                        <input class="input ${not empty errors.password ? 'error' : ''}"
                               id="password" name="password" type="password"
                               placeholder="设置登录密码" autocomplete="new-password">
                        <button type="button" class="toggle-password" data-toggle-password="password">显示</button>
                    </div>
                    <c:if test="${not empty errors.password}">
                        <span class="error-text"><c:out value="${errors.password}"/></span>
                    </c:if>
                </div>

                <div class="field">
                    <label for="confirmPassword">确认密码</label>
                    <div class="password-wrap">
                        <input class="input ${not empty errors.confirmPassword ? 'error' : ''}"
                               id="confirmPassword" name="confirmPassword" type="password"
                               placeholder="再输入一次密码" autocomplete="new-password">
                        <button type="button" class="toggle-password" data-toggle-password="confirmPassword">显示</button>
                    </div>
                    <c:if test="${not empty errors.confirmPassword}">
                        <span class="error-text"><c:out value="${errors.confirmPassword}"/></span>
                    </c:if>
                </div>

                <button type="submit" class="btn btn-primary auth-submit">创建账号 →</button>
            </form>

            <p class="auth-switch">已经有账号了？<a href="${ctx}/login.jsp">返回登录</a></p>
            <p class="auth-mini-footer">注册即表示同意仅将信息用于本课程设计系统</p>
        </div>
    </main>
</div>
<script src="${ctx}/assets/js/app.js?v=18"></script>
</body>
</html>
