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
    <link rel="stylesheet" href="${ctx}/assets/css/app.css?v=18">
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
    <nav class="breadcrumb" aria-label="面包屑">
        <a href="${ctx}/home">首页</a>
        <span class="sep">/</span>
        <a href="${ctx}/category?id=${book.categoryId}"><c:out value="${book.categoryName}"/></a>
        <span class="sep">/</span>
        <span><c:out value="${book.name}"/></span>
    </nav>

    <div class="detail-grid">
        <div class="detail-cover-wrap">
            <div class="detail-cover cat-${book.categoryColor}" data-cover-book="${book.id}">
                <span class="cover-initial">${book.initial}</span>
                <c:if test="${book.newArrival}">
                    <span class="cover-new">新<i>✦</i></span>
                </c:if>
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
                <span class="badge status-${fn:toLowerCase(book.status)}">
                    <c:choose>
                        <c:when test="${book.pending}">⏳ 待审核</c:when>
                        <c:when test="${book.rejected}">✖ 未通过审核</c:when>
                        <c:otherwise>✔ 已上架</c:otherwise>
                    </c:choose>
                </span>
                <c:choose>
                    <c:when test="${not empty book.contentFile}">
                        <span class="badge read">📖 可在线阅读</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge">暂未收录正文</span>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="rating-summary">
                <span class="stars-avg" title="平均分 ${avgStars}">
                    <span class="stars-base">★★★★★</span>
                    <span class="stars-fill" style="width:${avgStars / 5 * 100}%">★★★★★</span>
                </span>
                <strong>${empty book.avgStarsLabel ? '暂无评分' : book.avgStarsLabel}</strong>
                <span class="rating-count">${book.ratingCount} 人评分</span>
            </div>

            <c:if test="${not empty book.contentFile}">
                <div class="detail-read-row">
                    <a class="btn btn-primary detail-read-cta" href="${ctx}/read?id=${book.id}">开始阅读 →</a>
                    <span class="detail-read-tip">在线阅读 · 支持批注与章节评论</span>
                </div>
            </c:if>

            <c:if test="${not empty book.uploaderName}">
                <p class="uploader-line">上传者：<c:out value="${book.uploaderName}"/></p>
            </c:if>

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

            <div class="rate-box">
                <div class="rate-head">
                    <strong>${myStars > 0 ? '我的评分' : '给这本书打分'}</strong>
                    <span>${myStars > 0 ? '已评 ' : ''}${myStars > 0 ? myStars : ''}${myStars > 0 ? ' 星，可重新选择' : '满分 5 星'}</span>
                </div>
                <form action="${ctx}/rating" method="post" class="star-form">
                    <input type="hidden" name="bookId" value="${book.id}">
                    <div class="star-input">
                        <c:forEach var="i" begin="1" end="5" varStatus="st">
                            <c:set var="s" value="${6 - st.index}"/>
                            <input type="radio" id="star-${s}" name="stars" value="${s}"
                                   <c:if test="${myStars == s}">checked</c:if> required>
                            <label for="star-${s}" title="${s} 星">★</label>
                        </c:forEach>
                    </div>
                    <button type="submit" class="btn btn-primary btn-sm">提交评分</button>
                </form>
            </div>

            <div class="detail-actions">
                <c:choose>
                    <c:when test="${not empty book.contentFile}">
                        <a class="btn btn-primary btn-sm" data-read-link data-book-id="${book.id}"
                           href="${ctx}/read?id=${book.id}">开始阅读</a>
                    </c:when>
                    <c:when test="${loginUser.admin}">
                        <a class="btn btn-soft btn-sm" href="${ctx}/book?action=toEdit&id=${book.id}">上传正文</a>
                    </c:when>
                </c:choose>
                <c:if test="${loginUser.admin}">
                    <a class="btn btn-ghost btn-sm" href="${ctx}/book?action=toEdit&id=${book.id}">编辑图书</a>
                </c:if>
                <a class="btn btn-ghost btn-sm" href="${ctx}/category?id=${book.categoryId}">返回书架</a>
                <c:if test="${loginUser.admin or book.uploaderId == loginUser.id}">
                    <form action="${ctx}/book" method="post" style="margin-left:auto"
                          data-confirm="确定删除《${fn:escapeXml(book.name)}》吗？此操作不可撤销。">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${book.id}">
                        <button class="btn btn-danger-soft btn-sm" type="submit">删除这本书</button>
                    </form>
                </c:if>
            </div>
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
