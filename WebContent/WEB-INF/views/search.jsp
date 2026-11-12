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
    <link rel="stylesheet" href="${ctx}/assets/css/app.css">
</head>
<body>
<%@ include file="common/header.jspf" %>

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
            <p>跨板块检索书名、作者、ISBN 与出版社</p>
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
        <form class="select-wrap" action="${ctx}/search" method="get">
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
                        <%@ include file="common/book-card.jspf" %>
                    </div>
                </c:forEach>
            </div>

            <c:if test="${bookPage.totalPages > 1}">
                <c:url var="prevUrl" value="/search">
                    <c:param name="keyword" value="${keyword}"/>
                    <c:param name="sort" value="${sort}"/>
                    <c:param name="page" value="${bookPage.prevPage}"/>
                </c:url>
                <c:url var="nextUrl" value="/search">
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
