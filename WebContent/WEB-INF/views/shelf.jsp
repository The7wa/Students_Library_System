<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${category.name}"/> · 书架 · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css">
</head>
<body>
<%@ include file="common/header.jspf" %>

<main class="page">
    <nav class="breadcrumb" aria-label="面包屑">
        <a href="${ctx}/home">首页</a>
        <span class="sep">/</span>
        <span><c:out value="${category.name}"/></span>
    </nav>

    <section class="shelf-hero cat-${category.color}">
        <div class="shelf-icon">${category.icon}</div>
        <div class="shelf-copy">
            <span class="kicker">COLLECTION</span>
            <h1><c:out value="${category.name}"/></h1>
            <p><c:out value="${category.description}"/></p>
            <div class="shelf-meta">
                <span><i></i>${category.bookCount} 种图书</span>
                <span><i></i>按主题整理</span>
                <span><i></i>支持关键词检索与排序</span>
            </div>
        </div>
        <div class="shelf-actions">
            <a class="btn btn-ghost btn-sm" href="${ctx}/category?action=toEdit&id=${category.id}">编辑板块</a>
            <form action="${ctx}/category" method="post"
                  data-confirm="删除板块「${fn:escapeXml(category.name)}」会同时删除其中 ${category.bookCount} 本图书，确定继续吗？">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="id" value="${category.id}">
                <button class="btn btn-danger-soft btn-sm" type="submit">删除板块</button>
            </form>
            <a class="btn btn-primary btn-sm" href="${ctx}/book?action=toAdd&categoryId=${category.id}">＋ 新增图书</a>
        </div>
    </section>

    <nav class="category-pills" aria-label="板块切换">
        <a class="pill ${empty activeCategoryId ? 'active' : ''}" href="${ctx}/search">全部图书</a>
        <c:forEach var="cat" items="${categories}">
            <a class="${activeCategoryId == cat.id ? 'active ' : ''}pill cat-${cat.color}"
               href="${ctx}/category?id=${cat.id}">
                ${cat.icon} <c:out value="${cat.name}"/>
                <span class="count">${cat.bookCount}</span>
            </a>
        </c:forEach>
    </nav>

    <div class="toolbar">
        <div class="toolbar-title">
            <strong>书架</strong>
            <span>共 ${bookPage.total} 种图书</span>
        </div>

        <form class="search-inline" action="${ctx}/category" method="get">
            <input type="hidden" name="id" value="${category.id}">
            <input type="hidden" name="sort" value="${fn:escapeXml(sort)}">
            <input type="text" name="keyword" value="${fn:escapeXml(keyword)}"
                   placeholder="在本板块内搜索..." aria-label="板块内搜索">
            <button type="submit">搜索</button>
        </form>

        <form class="select-wrap" action="${ctx}/category" method="get">
            <input type="hidden" name="id" value="${category.id}">
            <input type="hidden" name="keyword" value="${fn:escapeXml(keyword)}">
            <select class="select" name="sort" data-autosubmit aria-label="排序方式">
                <option value="newest" ${sort eq 'newest' or empty sort ? 'selected' : ''}>最近上架</option>
                <option value="oldest" ${sort eq 'oldest' ? 'selected' : ''}>最早上架</option>
                <option value="nameAsc" ${sort eq 'nameAsc' ? 'selected' : ''}>书名 A-Z</option>
            </select>
        </form>
    </div>

    <c:choose>
        <c:when test="${empty bookPage.items}">
            <div class="empty">
                <div class="empty-icon">📖</div>
                <h3>这个书架还没有书</h3>
                <p>
                    <c:choose>
                        <c:when test="${not empty keyword}">没有找到和「<c:out value="${keyword}"/>」相关的图书，换个关键词试试。</c:when>
                        <c:otherwise>把这个板块的第一本书放进来，书架就活了。</c:otherwise>
                    </c:choose>
                </p>
                <a class="btn btn-primary btn-sm" href="${ctx}/book?action=toAdd&categoryId=${category.id}">上架第一本书</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="book-grid">
                <c:forEach var="book" items="${bookPage.items}" varStatus="st">
                    <div style="display:contents;--i:${st.index}">
                        <%@ include file="common/book-card.jspf" %>
                    </div>
                </c:forEach>
            </div>

            <c:if test="${bookPage.totalPages > 1}">
                <c:url var="prevUrl" value="/category">
                    <c:param name="id" value="${category.id}"/>
                    <c:param name="keyword" value="${keyword}"/>
                    <c:param name="sort" value="${sort}"/>
                    <c:param name="page" value="${bookPage.prevPage}"/>
                </c:url>
                <c:url var="nextUrl" value="/category">
                    <c:param name="id" value="${category.id}"/>
                    <c:param name="keyword" value="${keyword}"/>
                    <c:param name="sort" value="${sort}"/>
                    <c:param name="page" value="${bookPage.nextPage}"/>
                </c:url>

                <div class="pagination">
                    <c:choose>
                        <c:when test="${bookPage.hasPrev}"><a href="${prevUrl}">← 上一页</a></c:when>
                        <c:otherwise><span class="disabled">← 上一页</span></c:otherwise>
                    </c:choose>
                    <span class="page-info">第 <b>${bookPage.page}</b> / ${bookPage.totalPages} 页</span>
                    <c:choose>
                        <c:when test="${bookPage.hasNext}"><a href="${nextUrl}">下一页 →</a></c:when>
                        <c:otherwise><span class="disabled">下一页 →</span></c:otherwise>
                    </c:choose>
                </div>
            </c:if>
        </c:otherwise>
    </c:choose>
</main>

<%@ include file="common/footer.jspf" %>
</body>
</html>
