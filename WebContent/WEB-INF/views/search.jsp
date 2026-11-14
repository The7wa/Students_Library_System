<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="activeNav" value="search"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${empty keyword ? '全部图书' : '搜索结果'} · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css?v=20">
</head>
<body><script>(function(){try{var t=localStorage.getItem("siteTheme")||localStorage.getItem("readerTheme");if(t==="green"||t==="black"){document.body.className+=(document.body.className?" ":"")+"theme-"+t;}}catch(e){}})();</script>
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
    <section class="search-head">
        <div>
            <span class="kicker">SEARCH THE LIBRARY</span>
            <h1>
                <c:choose>
                    <c:when test="${empty keyword}">全部图书</c:when>
                    <c:otherwise>“<em><c:out value="${keyword}"/></em>” 的搜索结果</c:otherwise>
                </c:choose>
            </h1>
            <p>跨板块检索书名、作者与出版社</p>
        </div>
        <form class="search-inline" action="${ctx}/search" method="get">
            <input type="hidden" name="sort" value="${fn:escapeXml(sort)}">
            <input type="text" name="keyword" value="${fn:escapeXml(keyword)}"
                   placeholder="输入关键词..." aria-label="全馆搜索">
            <button type="submit">搜索</button>
        </form>
    </section>

    <nav class="category-pills" aria-label="按板块筛选">
        <a class="pill ${empty keyword and empty param.categoryId ? 'active' : ''}" href="${ctx}/search">全部</a>
        <c:forEach var="cat" items="${categories}">
            <a class="pill cat-${cat.color}" href="${ctx}/category?id=${cat.id}">
                ${cat.icon} <c:out value="${cat.name}"/>
            </a>
        </c:forEach>
    </nav>

    <div class="toolbar">
        <div class="toolbar-title">
            <strong>检索结果</strong>
            <span>共 ${bookPage.total} 条</span>
        </div>
        <c:if test="${not emptySearch and not empty bookPage.items}">
            <form class="select-wrap" action="${ctx}/search" method="get">
                <input type="hidden" name="keyword" value="${fn:escapeXml(keyword)}">
                <select class="select" name="sort" data-autosubmit aria-label="排序方式">
                    <option value="newest" ${sort eq 'newest' or empty sort ? 'selected' : ''}>最近上架</option>
                    <option value="oldest" ${sort eq 'oldest' ? 'selected' : ''}>最早上架</option>
                    <option value="nameAsc" ${sort eq 'nameAsc' ? 'selected' : ''}>书名 A-Z</option>
                </select>
            </form>
        </c:if>
    </div>

    <c:choose>
        <c:when test="${emptySearch}">
            <div class="empty-search">
                <div class="empty-icon">🔍</div>
                <h3>请输入搜索关键字</h3>
                <p>输入书名、作者或出版社名称进行检索</p>
            </div>
        </c:when>
        <c:when test="${empty bookPage.items}">
            <div class="empty">
                <div class="empty-icon">🔍</div>
                <h3>没有找到这本书</h3>
                <p>试试更短的关键词，或者换一个书名、作者名再搜一次。</p>
                <a class="btn btn-primary btn-sm" href="${ctx}/search">查看全部图书</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="book-grid">
                <c:forEach var="book" items="${bookPage.items}" varStatus="st">
                    <div style="display:contents;--i:${st.index}">
                        <article class="book-card">
    <a class="book-cover cat-${book.categoryColor}" href="${ctx}/book?action=detail&id=${book.id}" data-cover-book="${book.id}">
        <span class="cover-shine"></span>
        <c:if test="${book.newArrival}">
            <span class="cover-new">新<i>✦</i></span>
        </c:if>
        <span class="cover-initial">${book.initial}</span>
        <span class="cover-title"><c:out value="${book.name}"/></span>
        <span class="cover-cat">${book.categoryIcon} <c:out value="${book.categoryName}"/></span>
    </a>
    <div class="book-info">
        <a class="book-title" href="${ctx}/book?action=detail&id=${book.id}">
            ${book.searchNameHtml}
        </a>
        <p class="book-author">
            ${book.searchAuthorHtml}
            <c:if test="${not empty book.publisher}">
                <span class="dot">·</span> ${book.searchPublisherHtml}
            </c:if>
        </p>
        <c:if test="${not empty keyword and not empty book.searchSnippetHtml}">
            <p class="book-snippet">${book.searchSnippetHtml}</p>
        </c:if>
        <div class="book-meta">
            <c:choose>
                <c:when test="${not empty book.contentFile}">
                    <span class="readable">可阅读</span>
                </c:when>
                <c:otherwise>
                    <span class="readable muted">暂无正文</span>
                </c:otherwise>
            </c:choose>
            <c:if test="${book.ratingCount > 0}">
                <span class="card-stars">★ ${book.avgStarsLabel}</span>
            </c:if>
        </div>
        <div class="book-actions">
            <a href="${ctx}/book?action=detail&id=${book.id}">详情</a>
            <c:if test="${loginUser.admin}">
                <a href="${ctx}/book?action=toEdit&id=${book.id}">编辑</a>
            </c:if>
            <c:if test="${loginUser.admin or book.uploaderId == loginUser.id}">
                <form action="${ctx}/book" method="post" data-confirm="确定删除《${fn:escapeXml(book.name)}》吗？此操作不可撤销。">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="id" value="${book.id}">
                    <button type="submit" class="link-danger">删除</button>
                </form>
            </c:if>
        </div>
    </div>
</article>

                    </div>
                </c:forEach>
            </div>

            <c:if test="${not empty bookPage.items}">
                <p class="the-end">-- 不要再扒拉我了 很疼得呦~ --</p>
            </c:if>
        </c:otherwise>
    </c:choose>
</main>

<footer class="site-footer">
    <div class="footer-inner">
        <p><span class="footer-brand">墨卷书阁</span> · 让每一本书都回到它的位置</p>
    </div>
</footer>
<script src="${ctx}/assets/js/app.js?v=18"></script>

</body>
</html>
