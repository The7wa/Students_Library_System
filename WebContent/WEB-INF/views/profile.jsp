<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>个人主页 · 墨卷书阁</title>
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
        <a href="${ctx}/home">首页</a><span class="sep">/</span><span>个人主页</span>
    </nav>

    <section class="profile-hero">
        <div class="profile-avatar"><c:out value="${profileUser.avatarText}"/></div>
        <div class="profile-copy">
            <span class="kicker">READER PROFILE</span>
            <h1><c:out value="${profileUser.displayName}"/></h1>
            <p class="profile-handle">@<c:out value="${profileUser.username}"/>
                <span class="badge">${profileUser.admin ? '管理员' : '读者'}</span>
                <span class="profile-joined">加入于 ${fn:substring(profileUser.createdAt, 0, 10)}</span>
            </p>
            <p class="profile-bio">
                <c:choose>
                    <c:when test="${not empty profileUser.bio}"><c:out value="${profileUser.bio}"/></c:when>
                    <c:otherwise>还没有填写个人简介。</c:otherwise>
                </c:choose>
            </p>
        </div>
        <div class="profile-actions">
            <a class="btn btn-ghost btn-sm" href="${ctx}/user?action=notes">我的笔记</a>
            <a class="btn btn-primary btn-sm" href="${ctx}/user?action=settings">设置</a>
        </div>
    </section>

    <section class="profile-stats">
        <div><strong>${uploadCount}</strong><span>上传图书</span></div>
        <div><strong>${annotationCount}</strong><span>批注笔记</span></div>
        <div><strong>${commentCount}</strong><span>发表评论</span></div>
        <div><strong>${ratingCount}</strong><span>评分记录</span></div>
    </section>

    <div class="profile-grid">
        <section class="form-card profile-edit">
            <div class="form-head">
                <span class="kicker">EDIT PROFILE</span>
                <h2>编辑个人资料</h2>
                <p>昵称、头像和简介会显示在批注、评论等公开位置。</p>
            </div>
            <form action="${ctx}/user" method="post">
                <input type="hidden" name="action" value="updateProfile">
                <div class="form-grid">
                    <div class="field">
                        <label for="nickname">昵称 <span class="req">*</span></label>
                        <input class="input" id="nickname" name="nickname" maxlength="20"
                               value="${fn:escapeXml(profileUser.nickname)}" required>
                    </div>
                    <div class="field">
                        <label for="email">邮箱</label>
                        <input class="input" id="email" name="email" type="email" maxlength="100"
                               value="${fn:escapeXml(profileUser.email)}" placeholder="you@example.com">
                    </div>
                    <div class="field field-full">
                        <label>头像</label>
                        <div class="avatar-picker">
                            <label class="avatar-option ${empty profileUser.avatar ? 'selected' : ''}">
                                <input type="radio" name="avatar" value="" ${empty profileUser.avatar ? 'checked' : ''}>
                                <span class="avatar avatar-sample"><c:out value="${fn:substring(profileUser.displayName, 0, 1)}"/></span>
                                <small>默认</small>
                            </label>
                            <c:forTokens var="emoji" items="😀,😎,🦊,🐼,🐱,🐶,🦉,🐳,🌿,🍀,☕,🎧,📚,✨,🌙,🍂" delims=",">
                                <label class="avatar-option ${profileUser.avatar eq emoji ? 'selected' : ''}">
                                    <input type="radio" name="avatar" value="${emoji}" ${profileUser.avatar eq emoji ? 'checked' : ''}>
                                    <span class="avatar avatar-sample">${emoji}</span>
                                </label>
                            </c:forTokens>
                        </div>
                    </div>
                    <div class="field field-full">
                        <label for="bio">个人简介 <span class="hint">最多 200 字</span></label>
                        <textarea class="textarea" id="bio" name="bio" maxlength="200"
                                  placeholder="介绍一下你的阅读口味……"><c:out value="${profileUser.bio}"/></textarea>
                    </div>
                </div>
                <div class="form-actions">
                    <span class="spacer">@${profileUser.username}</span>
                    <button type="submit" class="btn btn-primary">保存资料</button>
                </div>
            </form>
        </section>

        <aside class="profile-side">
            <div class="side-card">
                <div class="side-head">
                    <h3>最近笔记</h3>
                    <a href="${ctx}/user?action=notes">全部 →</a>
                </div>
                <c:choose>
                    <c:when test="${empty recentNotes}">
                        <p class="side-empty">还没有批注，去阅读页选中一段文字试试。</p>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="n" items="${recentNotes}" varStatus="st">
                            <c:if test="${st.index < 3}">
                                <a class="side-note" href="${ctx}/read?id=${n.bookId}&chapter=${n.chapterIndex}">
                                    <span class="note-color" style="background:${n.color}"></span>
                                    <span>
                                        <strong><c:out value="${n.quote}"/></strong>
                                        <small><c:out value="${n.bookName}"/> · 第 ${n.chapterIndex + 1} 章</small>
                                    </span>
                                </a>
                            </c:if>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </aside>
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
