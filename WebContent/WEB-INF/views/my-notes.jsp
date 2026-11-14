<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>我的笔记 · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css?v=19">
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
        <a href="${ctx}/user?action=profile">个人主页</a><span class="sep">/</span><span>我的笔记</span>
    </nav>

    <section class="notes-page-head">
        <div>
            <span class="kicker">MY HIGHLIGHTS</span>
            <h1>我的笔记</h1>
            <p>把你在书里划过的重点和写下的想法汇总在一起；同一段内容只保留最后一次的颜色和笔记。</p>
        </div>
        <div class="notes-count"><strong>${noteCount}</strong><span>条笔记</span></div>
    </section>

    <c:choose>
        <c:when test="${empty notes}">
            <div class="empty">
                <div class="empty-icon">✎</div>
                <h3>还没有笔记</h3>
                <p>打开一本有正文的书，选中一段文字，就可以留下彩色批注。</p>
                <a class="btn btn-primary btn-sm" href="${ctx}/search">去找一本书</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="my-notes-list">
                <c:forEach var="n" items="${notes}">
                    <article class="my-note-card cat-${n.bookColor}">
                        <div class="my-note-main">
                            <div class="my-note-head">
                                <c:choose>
                                    <c:when test="${n.bookHasContent}">
                                        <a class="my-note-book" href="${ctx}/read?id=${n.bookId}&chapter=${n.chapterIndex}">
                                            <c:out value="${n.bookName}"/>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <a class="my-note-book" href="${ctx}/book?action=detail&id=${n.bookId}">
                                            <c:out value="${n.bookName}"/>
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                                <span>第 ${n.chapterIndex + 1} 章</span>
                                <span>${fn:substring(n.createdAt, 0, 16)}</span>
                            </div>
                            <blockquote class="my-note-quote" style="border-color:${n.color}">
                                “<c:out value="${n.quote}"/>”
                            </blockquote>
                            <c:choose>
                                <c:when test="${not empty n.note}">
                                    <p class="my-note-text"><c:out value="${n.note}"/></p>
                                </c:when>
                                <c:otherwise>
                                    <p class="my-note-text muted">仅颜色标记，没有文字笔记</p>
                                </c:otherwise>
                            </c:choose>
                            <div class="my-note-foot">
                                <span class="note-color" style="background:${n.color}"></span>
                                <c:choose>
                                    <c:when test="${n.bookHasContent}">
                                        <a href="${ctx}/read?id=${n.bookId}&chapter=${n.chapterIndex}#anno-${n.id}">回到原文 →</a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${ctx}/book?action=detail&id=${n.bookId}">查看图书 →</a>
                                    </c:otherwise>
                                </c:choose>
                            </div>
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
<script src="${ctx}/assets/js/app.js?v=18"></script>
</body>
</html>
