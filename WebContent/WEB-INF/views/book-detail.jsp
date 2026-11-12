<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${book.name}"/> · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css">
</head>
<body>
<%@ include file="common/header.jspf" %>

<main class="page">
    <nav class="breadcrumb" aria-label="面包屑">
        <a href="${ctx}/home">首页</a>
        <span class="sep">/</span>
        <a href="${ctx}/category?id=${book.categoryId}"><c:out value="${book.categoryName}"/></a>
        <span class="sep">/</span>
        <span><c:out value="${book.name}"/></span>
    </nav>

    <div class="detail-grid">
        <div class="detail-cover-wrap">
            <div class="detail-cover cat-${book.categoryColor}">
                <span class="cover-initial">${book.initial}</span>
                <span class="detail-cover-title"><c:out value="${book.name}"/></span>
                <span class="detail-cover-author"><c:out value="${book.author}"/> 著</span>
            </div>
        </div>

        <section class="detail-panel cat-${book.categoryColor}">
            <span class="kicker">BOOK DETAIL</span>
            <h1><c:out value="${book.name}"/></h1>
            <p class="detail-sub"><c:out value="${book.author}"/> · <c:out value="${book.publisher}"/></p>

            <div class="detail-badges">
                <span class="badge cat">${book.categoryIcon} <c:out value="${book.categoryName}"/></span>
                <c:choose>
                    <c:when test="${not empty book.contentFile}">
                        <span class="badge read">📖 可在线阅读</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge">暂未收录正文</span>
                    </c:otherwise>
                </c:choose>
            </div>

            <dl class="meta-list">
                <div>
                    <dt>作者</dt>
                    <dd><c:out value="${book.author}"/></dd>
                </div>
                <div>
                    <dt>出版社</dt>
                    <dd><c:out value="${book.publisher}"/></dd>
                </div>
                <div>
                    <dt>ISBN</dt>
                    <dd><c:out value="${book.isbn}"/></dd>
                </div>
                <div>
                    <dt>正文</dt>
                    <dd>
                        <c:choose>
                            <c:when test="${not empty book.contentFile}">已收录，可在线阅读</c:when>
                            <c:otherwise>尚未收录</c:otherwise>
                        </c:choose>
                    </dd>
                </div>
                <div>
                    <dt>上架时间</dt>
                    <dd>${fn:substring(book.createTime, 0, 10)}</dd>
                </div>
                <div>
                    <dt>最近更新</dt>
                    <dd>${fn:substring(book.updateTime, 0, 10)}</dd>
                </div>
            </dl>

            <div class="description-block">
                <h3>内容简介</h3>
                <c:choose>
                    <c:when test="${empty book.description}">
                        <p>这本书还没有填写简介。</p>
                    </c:when>
                    <c:otherwise>
                        <p><c:out value="${book.description}"/></p>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="detail-actions">
                <c:choose>
                    <c:when test="${not empty book.contentFile}">
                        <a class="btn btn-primary btn-sm" data-read-link data-book-id="${book.id}"
                           href="${ctx}/read?id=${book.id}">开始阅读</a>
                        <a class="btn btn-ghost btn-sm" href="${ctx}/book?action=toEdit&id=${book.id}">编辑图书</a>
                    </c:when>
                    <c:otherwise>
                        <a class="btn btn-soft btn-sm" href="${ctx}/book?action=toEdit&id=${book.id}">上传正文</a>
                    </c:otherwise>
                </c:choose>
                <a class="btn btn-ghost btn-sm" href="${ctx}/category?id=${book.categoryId}">返回书架</a>
                <form action="${ctx}/book" method="post" style="margin-left:auto"
                      data-confirm="确定删除《${fn:escapeXml(book.name)}》吗？此操作不可撤销。">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="id" value="${book.id}">
                    <button class="btn btn-danger-soft btn-sm" type="submit">删除这本书</button>
                </form>
            </div>
        </section>
    </div>
</main>

<%@ include file="common/footer.jspf" %>
</body>
</html>
