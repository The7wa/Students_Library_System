<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>通知 · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css?v=16?v=6">
</head>
<body><script>(function(){try{var t=localStorage.getItem("siteTheme")||localStorage.getItem("readerTheme");if(t==="green"||t==="black"){document.body.className+=(document.body.className?" ":"")+"theme-"+t;}}catch(e){}})();</script>
<header class="topbar">
    <div class="topbar-inner">
        <a class="brand" href="${ctx}/home" aria-label="墨卷书阁首页">
            <span class="brand-mark">墨</span>
            <span class="brand-text"><strong>墨卷书阁</strong><small>LIBRARY SYSTEM</small></span>
        </a>
        <form class="global-search" action="${ctx}/search" method="get" role="search">
            <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="7"></circle><path d="M20 20l-3.2-3.2"></path></svg>
            <input type="text" name="keyword" value="${fn:escapeXml(keyword)}" placeholder="搜索书名、作者或出版社" autocomplete="off">
            <button type="submit">检索</button>
        </form>
        <nav class="topnav" aria-label="主导航">
            <a class="nav-link" href="${ctx}/home">首页</a>
            <a class="nav-link" href="${ctx}/search">全部图书</a>
            <a class="nav-link" href="${ctx}/book?action=myUploads">我的上传</a>
            <a class="nav-link active" href="${ctx}/notification">通知<c:if test="${unreadNotifCount > 0}"><b class="notif-badge">${unreadNotifCount}</b></c:if></a>
            <c:if test="${loginUser.admin}">
                <a class="nav-link" href="${ctx}/admin">管理<c:if test="${pendingBookCount > 0}"><b class="notif-badge">${pendingBookCount}</b></c:if></a>
            </c:if>
            <span class="nav-divider"></span>
            <span class="user-chip" title="${fn:escapeXml(loginUser.displayName)}">
                <span class="avatar">${fn:substring(loginUser.displayName, 0, 1)}</span>
                <span class="user-meta"><strong><c:out value="${loginUser.displayName}"/></strong><small>${loginUser.admin ? '管理员' : '读者'}</small></span>
            </span>
            <a class="logout" href="${ctx}/auth?action=logout" title="退出登录">退出</a>
        </nav>
    </div>
</header>

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
    <div class="page-head">
        <div>
            <span class="kicker">NOTIFICATIONS</span>
            <h1>通知</h1>
            <p>管理员会收到系统变化通知；读者会收到自己上传作品的审核进度。</p>
        </div>
        <div class="notif-actions">
            <form action="${ctx}/notification" method="post">
                <input type="hidden" name="action" value="readAll">
                <button class="btn btn-ghost btn-sm" type="submit">全部标记已读</button>
            </form>
            <c:if test="${not empty notifications}">
                <form action="${ctx}/notification" method="post"
                      data-confirm="确定清空所有通知吗？此操作不可恢复。">
                    <input type="hidden" name="action" value="clearAll">
                    <button class="btn btn-danger-soft btn-sm" type="submit">清空通知</button>
                </form>
            </c:if>
        </div>
    </div>

    <c:choose>
        <c:when test="${empty notifications}">
            <div class="empty">
                <div class="empty-icon">🔔</div>
                <h3>暂时没有通知</h3>
                <p>有新的上传、审核结果或系统变化时会出现在这里。</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="notif-list">
                <c:forEach var="n" items="${notifications}">
                    <article class="notif-item ${n.read ? '' : 'unread'} type-${fn:toLowerCase(n.type)}">
                        <span class="notif-type type-${fn:toLowerCase(n.type)}">${n.typeLabel}</span>
                        <div class="notif-body">
                            <p><c:out value="${n.message}"/></p>
                            <span class="notif-time">${fn:substring(n.createdAt, 0, 16)}</span>
                        </div>
                        <c:choose>
                            <c:when test="${not empty n.link}">
                                <c:url var="readUrl" value="/notification">
                                    <c:param name="action" value="read"/>
                                    <c:param name="id" value="${n.id}"/>
                                    <c:param name="target" value="${n.link}"/>
                                </c:url>
                                <a class="notif-open" href="${readUrl}">查看 →</a>
                            </c:when>
                            <c:otherwise>
                                <c:url var="readUrl" value="/notification">
                                    <c:param name="action" value="read"/>
                                    <c:param name="id" value="${n.id}"/>
                                </c:url>
                                <a class="notif-open" href="${readUrl}">标记已读</a>
                            </c:otherwise>
                        </c:choose>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<footer class="site-footer">
    <div class="footer-inner">
        <p><span class="footer-brand">墨卷书阁</span> · 让每一本书都回到它的位置</p>
    </div>
</footer>
<script src="${ctx}/assets/js/app.js?v=16"></script>
</body>
</html>
