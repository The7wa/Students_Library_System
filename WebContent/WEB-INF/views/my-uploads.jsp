<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>我的上传 · 墨卷书阁</title>
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
            <a class="nav-link active" href="${ctx}/book?action=myUploads">我的上传</a>
            <a class="nav-link" href="${ctx}/notification">通知<c:if test="${unreadNotifCount > 0}"><b class="notif-badge">${unreadNotifCount}</b></c:if></a>
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
            <span class="kicker">MY UPLOADS</span>
            <h1>我的上传</h1>
            <p>上传后会进入待审核状态，管理员通过后才会出现在书架上；进度通知会发送到“通知”。</p>
        </div>
        <a class="btn btn-primary btn-sm" href="${ctx}/book?action=toAdd">＋ 上传新书</a>
    </div>

    <c:choose>
        <c:when test="${empty books}">
            <div class="empty">
                <div class="empty-icon">📤</div>
                <h3>还没有上传过图书</h3>
                <p>把你的藏书分享给书阁，管理员审核通过后大家就能读到了。</p>
                <a class="btn btn-primary btn-sm" href="${ctx}/book?action=toAdd">上传第一本书</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="upload-list">
                <c:forEach var="b" items="${books}">
                    <article class="upload-item">
                        <div class="upload-cover cat-${b.categoryColor}">
                            <span>${b.initial}</span>
                        </div>
                        <div class="upload-info">
                            <h3><c:out value="${b.name}"/></h3>
                            <p><c:out value="${b.author}"/> · ${b.categoryIcon} <c:out value="${b.categoryName}"/></p>
                            <p class="upload-meta">上传于 ${fn:substring(b.createTime, 0, 16)}</p>
                        </div>
                        <div class="upload-status">
                            <span class="badge status-${fn:toLowerCase(b.status)}">
                                <c:choose>
                                    <c:when test="${b.pending}">⏳ 待审核</c:when>
                                    <c:when test="${b.rejected}">✖ 未通过</c:when>
                                    <c:otherwise>✔ 已上架</c:otherwise>
                                </c:choose>
                            </span>
                            <c:choose>
                                <c:when test="${b.pending}">
                                    <p class="status-tip">等待管理员审核中</p>
                                </c:when>
                                <c:when test="${b.rejected}">
                                    <p class="status-tip">管理员未通过，可查看通知了解原因</p>
                                </c:when>
                                <c:otherwise>
                                    <p class="status-tip">已出现在书架，可被所有读者阅读</p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="upload-actions">
                            <a class="btn btn-ghost btn-sm" href="${ctx}/book?action=detail&id=${b.id}">详情</a>
                            <c:if test="${not b.pending and not b.rejected and not empty b.contentFile}">
                                <a class="btn btn-primary btn-sm" href="${ctx}/read?id=${b.id}">阅读</a>
                            </c:if>
                            <form action="${ctx}/book" method="post" data-confirm="确定删除《${fn:escapeXml(b.name)}》吗？">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="id" value="${b.id}">
                                <button class="btn btn-danger-soft btn-sm" type="submit">删除</button>
                            </form>
                        </div>
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
