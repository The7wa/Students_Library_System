<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>我的书架 · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css?v=20">
</head>
<body>
<c:set var="activeNav" value="bookshelf"/>
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
        <a href="${ctx}/user?action=profile">个人主页</a><span class="sep">/</span><span>我的书架</span>
    </nav>

    <section class="shelf-page-head">
        <div>
            <span class="kicker">MY BOOKSHELF</span>
            <h1>我的书架</h1>
            <p>喜欢的书放在这里，下次打开直接接着上次的章节读。</p>
        </div>
        <div class="shelf-count"><strong>${shelfCount}</strong><span>本书</span></div>
    </section>

    <c:choose>
        <c:when test="${empty shelfBooks}">
            <div class="empty shelf-empty">
                <div class="empty-icon">📚</div>
                <h3>书架还是空的</h3>
                <p>去书库看看，遇到喜欢的书点「加入书架」，下次就能从这里继续读。</p>
                <a class="btn btn-primary btn-sm" href="${ctx}/search">去逛书库 →</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="shelf-grid">
                <c:forEach var="b" items="${shelfBooks}" varStatus="st">
                    <article class="shelf-card" style="--i:${st.index}">
                        <a class="shelf-cover cat-${b.categoryColor}" href="${ctx}/book?action=detail&id=${b.id}">
                            <span class="shelf-cover-initial"><c:out value="${b.initial}"/></span>
                            <c:if test="${b.newArrival}"><span class="cover-new">新<i>✦</i></span></c:if>
                            <span class="shelf-cover-icon"><c:out value="${b.categoryIcon}"/></span>
                        </a>
                        <div class="shelf-main">
                            <span class="shelf-cat"><c:out value="${b.categoryIcon}"/> <c:out value="${b.categoryName}"/></span>
                            <a class="shelf-title" href="${ctx}/book?action=detail&id=${b.id}"><c:out value="${b.name}"/></a>
                            <p class="shelf-author"><c:out value="${b.author}"/></p>
                            <div class="shelf-meta">
                                <span class="shelf-stars">★ ${b.avgStarsLabel}</span>
                                <c:if test="${b.ratingCount > 0}"><span>${b.ratingCount} 人评分</span></c:if>
                                <span>上次读到第 ${b.lastChapter + 1} 章</span>
                            </div>
                            <div class="shelf-actions">
                                <c:choose>
                                    <c:when test="${not empty b.contentFile}">
                                        <a class="btn btn-primary btn-sm" data-read-link data-book-id="${b.id}"
                                           href="${ctx}/read?id=${b.id}&chapter=${b.lastChapter}">继续阅读 →</a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="shelf-no-content">暂无正文</span>
                                    </c:otherwise>
                                </c:choose>
                                <a class="btn btn-ghost btn-sm" href="${ctx}/book?action=detail&id=${b.id}">图书详情</a>
                                <form action="${ctx}/bookshelf" method="post" class="shelf-remove"
                                      data-confirm="确定把《${fn:escapeXml(b.name)}》移出书架吗？">
                                    <input type="hidden" name="action" value="remove">
                                    <input type="hidden" name="bookId" value="${b.id}">
                                    <input type="hidden" name="next" value="/bookshelf">
                                    <button type="submit" class="shelf-remove-btn">移出</button>
                                </form>
                            </div>
                            <c:if test="${not empty b.shelfAddedAt}">
                                <span class="shelf-added">加入于 ${fn:substring(b.shelfAddedAt, 0, 10)}</span>
                            </c:if>
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
<script src="${ctx}/assets/js/app.js?v=20"></script>
</body>
</html>
