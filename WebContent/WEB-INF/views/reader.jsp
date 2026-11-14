<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${book.name}"/> · 第 ${chapterIndex + 1} 章 · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css?v=18">
</head>
<body class="reader-page"><script>(function(){try{var t=localStorage.getItem("siteTheme")||localStorage.getItem("readerTheme");if(t==="green"||t==="black"){document.body.className+=(document.body.className?" ":"")+"theme-"+t;}}catch(e){}})();</script>
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

<main class="page reader-page">
    <nav class="breadcrumb" aria-label="面包屑">
        <a href="${ctx}/home">首页</a>
        <span class="sep">/</span>
        <a href="${ctx}/category?id=${book.categoryId}"><c:out value="${book.categoryName}"/></a>
        <span class="sep">/</span>
        <a href="${ctx}/book?action=detail&id=${book.id}"><c:out value="${book.name}"/></a>
        <span class="sep">/</span>
        <span>第 ${chapterIndex + 1} 章</span>
    </nav>

    <div class="reader-head">
        <div>
            <h1><c:out value="${book.name}"/></h1>
            <p class="reader-author"><c:out value="${book.author}"/> 著 · 第 ${chapterIndex + 1} / ${chapterCount} 章</p>
        </div>
        <div class="reader-head-right">
            <div class="reader-tools">
                <div class="tool-group" title="调整正文字号">
                    <button type="button" class="tool-btn" data-font="-">A－</button>
                    <span class="tool-val" id="fontVal">17</span>
                    <button type="button" class="tool-btn" data-font="+">A＋</button>
                </div>
                <div class="tool-group" title="自动滚屏">
                    <button type="button" class="tool-btn tool-primary" id="autoScrollBtn">自动滚动</button>
                    <select class="tool-select" id="autoSpeed" title="滚动速度">
                        <option value="1">慢速</option>
                        <option value="2" selected>中速</option>
                        <option value="3">快速</option>
                        <option value="4">飞快</option>
                    </select>
                </div>
            </div>
            <div class="rating-summary small">
                <span class="stars-avg" title="平均分 ${avgStars}">
                    <span class="stars-base">★★★★★</span>
                    <span class="stars-fill" style="width:${avgStars / 5 * 100}%">★★★★★</span>
                </span>
                <strong>${empty book.avgStarsLabel ? '暂无评分' : book.avgStarsLabel}</strong>
                <span class="rating-count">${book.ratingCount} 人</span>
            </div>
            <a class="btn btn-ghost btn-sm" href="${ctx}/book?action=detail&id=${book.id}">详情 / 打分</a>
        </div>
    </div>

    <!-- 本章阅读进度条 -->
    <div class="chapter-progress" id="chapterProgress" title="本章阅读进度">
        <span class="chapter-progress-track"><i class="chapter-progress-bar" id="chapterProgressBar"></i></span>
        <span class="chapter-progress-text" id="chapterProgressText">本章 0%</span>
    </div>

    <div class="reader-layout">
        <aside class="reader-toc">
            <h3>目录</h3>
            <ol class="toc-list">
                <c:forEach var="ch" items="${chapters}" varStatus="st">
                    <li>
                        <a class="${st.index == chapterIndex ? 'active' : ''}"
                           href="${ctx}/read?id=${book.id}&chapter=${st.index}">
                            <span>${st.index + 1}</span>
                            <c:out value="${ch.title}"/>
                        </a>
                    </li>
                </c:forEach>
            </ol>
        </aside>

        <article class="reader-main">
            <div class="chapter-head">
                <span class="chapter-no">第 ${chapterIndex + 1} 章</span>
                <h2><c:out value="${chapter.title}"/></h2>
            </div>

            <div class="reader-text" id="readerText">
                <c:forEach var="para" items="${paragraphs}" varStatus="st">
                    <p class="rd-p" id="p-${st.index}" data-p="${st.index}"><c:out value="${para}"/></p>
                </c:forEach>
            </div>

            <div class="reader-nav">
                <c:choose>
                    <c:when test="${hasPrev}">
                        <a class="btn btn-ghost btn-sm" href="${ctx}/read?id=${book.id}&chapter=${chapterIndex - 1}">← 上一章</a>
                    </c:when>
                    <c:otherwise><span class="btn btn-ghost btn-sm disabled">← 上一章</span></c:otherwise>
                </c:choose>
                <span class="reader-progress">${chapterIndex + 1} / ${chapterCount}</span>
                <c:choose>
                    <c:when test="${hasNext}">
                        <a class="btn btn-primary btn-sm" href="${ctx}/read?id=${book.id}&chapter=${chapterIndex + 1}">下一章 →</a>
                    </c:when>
                    <c:otherwise><span class="btn btn-primary btn-sm disabled">下一章 →</span></c:otherwise>
                </c:choose>
            </div>

            <section class="comments-section" id="comments">
                <div class="comments-head">
                    <h2>本章评论</h2>
                    <span>${comments.size()} 条</span>
                </div>

                <form action="${ctx}/comment" method="post" class="comment-form">
                    <input type="hidden" name="action" value="add">
                    <input type="hidden" name="bookId" value="${book.id}">
                    <input type="hidden" name="chapter" value="${chapterIndex}">
                    <textarea name="content" maxlength="1000" required
                              placeholder="说说你对第 ${chapterIndex + 1} 章的看法……"></textarea>
                    <div class="comment-form-foot">
                        <span>所有读者都可以看到你的评论</span>
                        <button type="submit" class="btn btn-primary btn-sm">发表评论</button>
                    </div>
                </form>

                <div class="comment-list">
                    <c:forEach var="cm" items="${comments}">
                        <div class="comment-item">
                            <div class="comment-avatar">${fn:substring(cm.displayName, 0, 1)}</div>
                            <div class="comment-body">
                                <div class="comment-head">
                                    <strong><c:out value="${cm.displayName}"/></strong>
                                    <span>${fn:substring(cm.createdAt, 0, 16)}</span>
                                </div>
                                <p><c:out value="${cm.content}"/></p>
                                <c:if test="${loginUser.admin or cm.userId == loginUser.id}">
                                    <form action="${ctx}/comment" method="post" class="comment-del"
                                          data-confirm="确定删除这条评论吗？">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${cm.id}">
                                        <input type="hidden" name="bookId" value="${book.id}">
                                        <input type="hidden" name="chapter" value="${chapterIndex}">
                                        <button type="submit">删除</button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty comments}">
                        <p class="comments-empty">还没有人评论，来抢沙发。</p>
                    </c:if>
                </div>
            </section>

            <section class="anno-public-section" id="annotations">
                <div class="anno-public-head">
                    <h2>书友批注</h2>
                    <span>${publicAnnotations.size()} 条公开标记 · 评论所有人都能看到</span>
                </div>
                <c:choose>
                    <c:when test="${empty publicAnnotations}">
                        <p class="anno-comments-empty">本章还没有公开批注，选中一段文字留下第一条吧。</p>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="a" items="${publicAnnotations}">
                            <article class="anno-card" id="anno-${a.id}">
                                <div class="anno-card-head">
                                    <span class="comment-avatar"><c:out value="${a.userAvatarText}"/></span>
                                    <div>
                                        <strong><c:out value="${a.userName}"/>${a.mine ? '（我）' : ''}</strong>
                                        <small>第 ${a.chapterIndex + 1} 章 · ${fn:substring(a.createdAt, 0, 16)}</small>
                                    </div>
                                    <span class="note-color" style="background:${a.color}"></span>
                                </div>
                                <blockquote class="anno-quote" style="border-color:${a.color}">
                                    “<c:out value="${a.quote}"/>”
                                </blockquote>
                                <c:if test="${a.mine and not empty a.note}">
                                    <p class="anno-note"><strong>我的笔记：</strong><c:out value="${a.note}"/></p>
                                </c:if>
                                <div class="anno-actions">
                                    <form action="${ctx}/annotation" method="post">
                                        <input type="hidden" name="action" value="like">
                                        <input type="hidden" name="annotationId" value="${a.id}">
                                        <button type="submit" class="anno-like ${a.likedByMe ? 'liked' : ''}">
                                            ${a.likedByMe ? '已赞' : '点赞'} ${a.likeCount}
                                        </button>
                                    </form>
                                    <span>${a.commentCount} 条评论</span>
                                </div>

                                <div class="anno-comments">
                                    <c:forEach var="cm" items="${commentsByAnnotation[a.id]}">
                                        <div class="anno-comment ${not empty cm.parentId ? 'reply' : ''}">
                                            <span class="comment-avatar"><c:out value="${cm.userAvatarText}"/></span>
                                            <div class="anno-comment-body">
                                                <div class="anno-comment-head">
                                                    <strong><c:out value="${cm.userName}"/>${cm.mine ? '（我）' : ''}</strong>
                                                    <span>${fn:substring(cm.createdAt, 0, 16)}</span>
                                                    <c:if test="${not empty cm.parentId}"><span>回复</span></c:if>
                                                </div>
                                                <p><c:out value="${cm.content}"/></p>
                                                <div class="anno-comment-tools">
                                                    <c:if test="${loginUser.admin or cm.mine}">
                                                        <form action="${ctx}/annotation" method="post"
                                                              data-confirm="确定删除这条评论吗？">
                                                            <input type="hidden" name="action" value="deleteComment">
                                                            <input type="hidden" name="id" value="${cm.id}">
                                                            <button type="submit" class="link-danger">删除</button>
                                                        </form>
                                                    </c:if>
                                                    <details class="reply-box">
                                                        <summary>回复</summary>
                                                        <form action="${ctx}/annotation" method="post" class="reply-form">
                                                            <input type="hidden" name="action" value="reply">
                                                            <input type="hidden" name="annotationId" value="${a.id}">
                                                            <input type="hidden" name="parentId" value="${cm.parentId == null ? cm.id : cm.parentId}">
                                                            <input type="text" name="content" maxlength="1000"
                                                                   placeholder="回复 ${fn:escapeXml(cm.userName)}..." required>
                                                            <button type="submit">发送</button>
                                                        </form>
                                                    </details>
                                                </div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                    <c:if test="${empty commentsByAnnotation[a.id]}">
                                        <p class="anno-comments-empty">还没有评论，来说两句。</p>
                                    </c:if>
                                    <form action="${ctx}/annotation" method="post" class="anno-comment-form">
                                        <input type="hidden" name="action" value="comment">
                                        <input type="hidden" name="annotationId" value="${a.id}">
                                        <textarea name="content" maxlength="1000" required
                                                  placeholder="对这段划线说点什么……"></textarea>
                                        <button type="submit">评论</button>
                                    </form>
                                </div>
                            </article>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </section>
        </article>

        <aside class="reader-notes">
            <div class="notes-head">
                <h3>我的批注</h3>
                <span><c:set var="chapterNoteCount" value="0"/><c:forEach var="a" items="${annotations}"><c:if test="${a.chapterIndex == chapterIndex}"><c:set var="chapterNoteCount" value="${chapterNoteCount + 1}"/></c:if></c:forEach>${chapterNoteCount} 条</span>
            </div>
            <p class="notes-tip">选中正文文字即可添加彩色笔记；批注只有你自己能看见。</p>

            <div class="note-list">
                <c:forEach var="a" items="${annotations}">
                    <c:if test="${a.chapterIndex == chapterIndex}">
                        <div class="note-item" data-target="p-${a.paragraphIndex}">
                            <span class="note-color" style="background:${a.color}"></span>
                            <div class="note-body">
                                <p class="note-quote">“<c:out value="${a.quote}"/>”</p>
                                <c:choose>
                                    <c:when test="${not empty a.note}">
                                        <p class="note-text"><c:out value="${a.note}"/></p>
                                    </c:when>
                                    <c:otherwise>
                                        <p class="note-text note-text-only">仅颜色标记</p>
                                    </c:otherwise>
                                </c:choose>
                                <div class="note-foot">
                                    <button type="button" class="note-jump" data-target="p-${a.paragraphIndex}">定位</button>
                                    <form action="${ctx}/annotation" method="post"
                                          data-confirm="确定删除这条批注吗？">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${a.id}">
                                        <input type="hidden" name="bookId" value="${book.id}">
                                        <input type="hidden" name="chapter" value="${chapterIndex}">
                                        <button type="submit" class="link-danger">删除</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>
                <c:if test="${chapterNoteCount == 0}">
                    <p class="notes-empty">还没有批注，选中一段文字试试。</p>
                </c:if>
            </div>
        </aside>
    </div>
</main>

<!-- 划选文字后的批注弹窗 -->
<div class="anno-popup" id="annoPopup" hidden>
    <form action="${ctx}/annotation" method="post" id="annoForm">
        <input type="hidden" name="action" value="add">
        <input type="hidden" name="bookId" value="${book.id}">
        <input type="hidden" name="chapter" value="${chapterIndex}">
        <input type="hidden" name="p" id="annoParagraph" value="0">
        <input type="hidden" name="quote" id="annoQuote" value="">
        <div class="anno-preview" id="annoPreview"></div>
        <div class="anno-colors" role="radiogroup" aria-label="笔记颜色">
            <label><input type="radio" name="color" value="#f5c242" checked><span style="background:#f5c242"></span></label>
            <label><input type="radio" name="color" value="#7bc77e"><span style="background:#7bc77e"></span></label>
            <label><input type="radio" name="color" value="#7fb2e5"><span style="background:#7fb2e5"></span></label>
            <label><input type="radio" name="color" value="#e58fb1"><span style="background:#e58fb1"></span></label>
            <label><input type="radio" name="color" value="#b79ce0"><span style="background:#b79ce0"></span></label>
        </div>
        <textarea name="note" id="annoNote" maxlength="1000"
                  placeholder="写下你的想法……（可以不填，仅做颜色标记）"></textarea>
        <div class="anno-actions">
            <button type="button" class="btn btn-ghost btn-sm" id="annoCancel">取消</button>
            <button type="submit" class="btn btn-primary btn-sm">保存批注</button>
        </div>
    </form>
</div>

<footer class="site-footer">
    <div class="footer-inner">
        <p><span class="footer-brand">墨卷书阁</span> · 让每一本书都回到它的位置</p>
    </div>
</footer>
<script>
    window.MY_ANNOTATIONS = ${annotationsJson};
    window.CHAPTER_INDEX = ${chapterIndex};
    window.BOOK_ID = ${book.id};
    window.CHAPTER_COUNT = ${chapterCount};
</script>
<script src="${ctx}/assets/js/reader.js?v=18"></script>
<script src="${ctx}/assets/js/app.js?v=18"></script>

</body>
</html>
