<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>设置 · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css?v=20">
</head>
<body>
<%@ include file="common/header.jspf" %>
<c:if test="${not empty sessionScope.flash}">
    <div class="flash-wrap">
        <div class="flash flash-${sessionScope.flash.type}" role="status">
            <span class="flash-dot"></span>
            <span class="flash-text"><c:out value="${sessionScope.flash.message}"/></span>
            <button type="button" class="flash-close" aria-label="关闭提示">×</button>
        </div>
    </div>
    <c:remove var="flash" scope="session"/>
</c:if>

<main class="page">
    <nav class="breadcrumb">
        <a href="${ctx}/home">首页</a><span class="sep">/</span>
        <a href="${ctx}/user?action=profile">个人主页</a><span class="sep">/</span><span>设置</span>
    </nav>

    <div class="settings-page">
        <div class="settings-head">
            <span class="kicker">PREFERENCES</span>
            <h1>设置</h1>
            <p>管理账户安全、界面语言、阅读主题和账户状态。</p>
        </div>

        <section class="settings-card">
            <div class="settings-card-head">
                <h2>账户与安全</h2>
                <p>修改密码后，下一次登录请使用新密码。</p>
            </div>
            <form action="${ctx}/user" method="post" class="settings-form">
                <input type="hidden" name="action" value="updatePassword">
                <div class="form-grid">
                    <div class="field field-full">
                        <label for="currentPassword">当前密码</label>
                        <input class="input" id="currentPassword" name="currentPassword" type="password"
                               autocomplete="current-password" required>
                    </div>
                    <div class="field">
                        <label for="newPassword">新密码 <span class="hint">6-32 位</span></label>
                        <input class="input" id="newPassword" name="newPassword" type="password"
                               autocomplete="new-password" required>
                    </div>
                    <div class="field">
                        <label for="confirmPassword">确认新密码</label>
                        <input class="input" id="confirmPassword" name="confirmPassword" type="password"
                               autocomplete="new-password" required>
                    </div>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">更新密码</button>
                </div>
            </form>
        </section>

        <section class="settings-card">
            <div class="settings-card-head">
                <h2>界面语言</h2>
                <p>切换后导航、按钮和账户页面会使用对应语言；图书与批注内容保持原文。</p>
            </div>
            <form action="${ctx}/user" method="post" class="language-form">
                <input type="hidden" name="action" value="language">
                <label class="language-option ${loginUser.lang ne 'en' ? 'active' : ''}">
                    <input type="radio" name="lang" value="zh" ${loginUser.lang ne 'en' ? 'checked' : ''}>
                    <span class="language-flag">中</span>
                    <span><strong>中文</strong><small>简体中文界面</small></span>
                </label>
                <label class="language-option ${loginUser.lang eq 'en' ? 'active' : ''}">
                    <input type="radio" name="lang" value="en" ${loginUser.lang eq 'en' ? 'checked' : ''}>
                    <span class="language-flag">EN</span>
                    <span><strong>English</strong><small>English interface</small></span>
                </label>
                <button type="submit" class="btn btn-soft btn-sm">应用语言</button>
            </form>
        </section>

        <section class="settings-card">
            <div class="settings-card-head">
                <h2>阅读主题</h2>
                <p>主题保存在当前浏览器，阅读页和书架都会跟随。</p>
            </div>
            <div class="theme-picker">
                <button type="button" class="theme-option" data-theme-set="">
                    <span class="theme-preview theme-preview-original"></span>
                    <strong>纸墨</strong>
                    <small>默认暖色</small>
                </button>
                <button type="button" class="theme-option" data-theme-set="green">
                    <span class="theme-preview theme-preview-green"></span>
                    <strong>青竹</strong>
                    <small>护眼绿色</small>
                </button>
                <button type="button" class="theme-option" data-theme-set="black">
                    <span class="theme-preview theme-preview-black"></span>
                    <strong>夜读</strong>
                    <small>深色模式</small>
                </button>
            </div>
        </section>

        <section class="settings-card danger-zone">
            <div class="settings-card-head">
                <h2>注销账户</h2>
                <p>注销后会删除你的批注、评论、点赞和评分；上传的图书会保留。此操作不可恢复。</p>
            </div>
            <c:choose>
                <c:when test="${loginUser.admin}">
                    <p class="settings-tip">管理员账号不能自行注销。</p>
                </c:when>
                <c:otherwise>
                    <form action="${ctx}/user" method="post" class="settings-form"
                          data-confirm="注销账户后无法恢复，确定继续吗？">
                        <input type="hidden" name="action" value="deleteAccount">
                        <div class="field">
                            <label for="deletePassword">输入密码确认注销</label>
                            <input class="input" id="deletePassword" name="password" type="password"
                                   autocomplete="current-password" required>
                        </div>
                        <div class="form-actions">
                            <button type="submit" class="btn btn-danger-soft">永久注销账户</button>
                        </div>
                    </form>
                </c:otherwise>
            </c:choose>
        </section>
    </div>
</main>

<footer class="site-footer">
    <div class="footer-inner">
        <p><span class="footer-brand">墨卷书阁</span> · 让每一本书都回到它的位置</p>
    </div>
</footer>
<script src="${ctx}/assets/js/app.js?v=18"></script>
</body>
</html>
