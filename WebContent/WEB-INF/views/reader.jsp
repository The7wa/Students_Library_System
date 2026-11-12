<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="theme-color" content="#f7f2e7">
    <title><c:out value="${chapter.title}"/> · <c:out value="${book.name}"/> · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css">
</head>
<body class="reader-page" data-theme="paper">
<div class="reader-topbar">
    <div class="reader-topbar-inner">
        <a class="reader-back" href="${ctx}/book?action=detail&id=${book.id}">
            <span aria-hidden="true">←</span> 书籍详情
        </a>

        <div class="reader-heading">
            <span class="reader-bookname"><c:out value="${book.name}"/></span>
            <span class="reader-meta">
                <c:out value="${book.author}"/> · 进度 ${chapterIndex + 1} / ${chapterCount}
            </span>
        </div>

        <div class="reader-tools">
            <button type="button" class="reader-tool" data-reader-font="-1" title="减小字号"
                    aria-label="减小字号">A−</button>
            <button type="button" class="reader-tool" data-reader-font="1" title="增大字号"
                    aria-label="增大字号">A+</button>
            <button type="button" class="reader-tool" data-reader-theme title="切换阅读背景"
                    aria-label="切换阅读背景"><span data-theme-icon>🌙</span></button>
            <button type="button" class="reader-tool reader-toc-toggle" data-toc-toggle
                    aria-label="打开目录">目录</button>
        </div>
    </div>
    <div class="reader-progress" aria-hidden="true"><span data-read-progress></span></div>
</div>

<div class="reader-shell">
    <aside class="reader-toc" id="readerToc" aria-label="章节目录">
        <div class="toc-head">
            <span>目录</span>
            <small>${chapterCount} 章</small>
        </div>
        <ol class="toc-list">
            <c:forEach var="ch" items="${chapters}">
                <li class="${ch.index == chapterIndex ? 'active' : ''}">
                    <a href="${ctx}/read?id=${book.id}&chapter=${ch.index}" data-chapter="${ch.index}">
                        <span class="toc-no">${ch.index + 1}</span>
                        <span class="toc-title"><c:out value="${ch.title}"/></span>
                    </a>
                </li>
            </c:forEach>
        </ol>
    </aside>
    <button type="button" class="reader-overlay" data-toc-close aria-label="关闭目录"></button>

    <main class="reader-main">
        <article class="reader-paper">
            <header class="reader-chapter-head">
                <span class="kicker">READING</span>
                <h1><c:out value="${chapter.title}"/></h1>
                <p>进度 ${chapterIndex + 1} / ${chapterCount} · ${chapter.charCount} 字</p>
            </header>

            <div class="reader-text" id="readerText" data-chapter-index="${chapterIndex}"><c:out value="${chapter.content}"/></div>

            <nav class="reader-chapter-nav" aria-label="章节翻页">
                <c:choose>
                    <c:when test="${hasPrev}">
                        <a class="reader-nav-btn" data-reader-prev
                           href="${ctx}/read?id=${book.id}&chapter=${chapterIndex - 1}">← 上一章</a>
                    </c:when>
                    <c:otherwise>
                        <span class="reader-nav-btn disabled">← 上一章</span>
                    </c:otherwise>
                </c:choose>

                <a class="reader-nav-btn center" href="${ctx}/book?action=detail&id=${book.id}">返回详情</a>

                <c:choose>
                    <c:when test="${hasNext}">
                        <a class="reader-nav-btn" data-reader-next
                           href="${ctx}/read?id=${book.id}&chapter=${chapterIndex + 1}">下一章 →</a>
                    </c:when>
                    <c:otherwise>
                        <span class="reader-nav-btn disabled">下一章 →</span>
                    </c:otherwise>
                </c:choose>
            </nav>
        </article>

        <p class="reader-tip">← / → 翻章 · 右上角可调字号与背景 · 阅读进度会自动记住</p>
    </main>
</div>

<script>
    window.LIB_READER = {
        bookId: ${book.id},
        chapter: ${chapterIndex},
        chapterCount: ${chapterCount}
    };
</script>
<script src="${ctx}/assets/js/reader.js"></script>
</body>
</html>
