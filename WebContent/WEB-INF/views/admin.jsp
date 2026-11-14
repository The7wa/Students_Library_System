<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>管理员后台 · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css?v=16?v=6">
</head>
<body><script>(function(){try{var t=localStorage.getItem("siteTheme")||localStorage.getItem("readerTheme");if(t==="green"||t==="black"){document.body.className+=(document.body.className?" ":"")+"theme-"+t;}}catch(e){}})();</script>
<header class="topbar">
    <div class="topbar-inner">
        <a class="brand" href="${ctx}/home" aria-label="墨卷书阁首页">
            <span class="brand-mark">墨</span>
            <span class="brand-text"><strong>墨卷书阁</strong><small>LIBRARY SYSTEM</small></span>
        </a>
        <form class="global-search" action="${ctx}/search" method="get" role="search">
            <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="7"></circle><path d="M20 20l-3.2-3.2"></path></svg>
            <input type="text" name="keyword" value="${fn:escapeXml(keyword)}" placeholder="搜索书名、作者或出版社" autocomplete="off">
            <button type="submit">检索</button>
        </form>
        <nav class="topnav" aria-label="主导航">
            <a class="nav-link" href="${ctx}/home">首页</a>
            <a class="nav-link" href="${ctx}/search">全部图书</a>
            <a class="nav-link" href="${ctx}/book?action=myUploads">我的上传</a>
            <a class="nav-link" href="${ctx}/notification">通知<c:if test="${unreadNotifCount > 0}"><b class="notif-badge">${unreadNotifCount}</b></c:if></a>
            <a class="nav-link active" href="${ctx}/admin">管理<c:if test="${pendingBookCount > 0}"><b class="notif-badge">${pendingBookCount}</b></c:if></a>
            <span class="nav-divider"></span>
            <span class="user-chip" title="${fn:escapeXml(loginUser.displayName)}">
                <span class="avatar">${fn:substring(loginUser.displayName, 0, 1)}</span>
                <span class="user-meta"><strong><c:out value="${loginUser.displayName}"/></strong><small>管理员</small></span>
            </span>
            <a class="logout" href="${ctx}/auth?action=logout" title="退出登录">退出</a>
        </nav>
    </div>
</header>

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
    <div class="page-head">
        <div>
            <span class="kicker">ADMIN CONSOLE</span>
            <h1>管理员后台</h1>
            <p>负责用户、板块、图书的管理；审核读者上传的作品。</p>
        </div>
        <div class="admin-stats">
            <span><b>${fn:length(users)}</b> 用户</span>
            <span><b>${fn:length(pendingBooks)}</b> 待审核</span>
            <span><b>${adminCount}</b> 管理员</span>
        </div>
    </div>

    <section class="admin-section">
        <div class="section-head compact">
            <div>
                <h2>待审核图书</h2>
                <p>通过后才会出现在书架；驳回会通知上传者。</p>
            </div>
        </div>
        <c:choose>
            <c:when test="${empty pendingBooks}">
                <div class="empty small"><p>暂无待审核图书。</p></div>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                        <tr><th>书名</th><th>作者</th><th>板块</th><th>上传者</th><th>时间</th><th>审核</th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="b" items="${pendingBooks}">
                            <tr>
                                <td><a href="${ctx}/book?action=detail&id=${b.id}"><c:out value="${b.name}"/></a></td>
                                <td><c:out value="${b.author}"/></td>
                                <td>${b.categoryIcon} <c:out value="${b.categoryName}"/></td>
                                <td><c:out value="${b.uploaderName}"/></td>
                                <td>${fn:substring(b.createTime, 0, 16)}</td>
                                <td class="review-cell">
                                    <form action="${ctx}/admin" method="post" class="inline-form">
                                        <input type="hidden" name="action" value="approve">
                                        <input type="hidden" name="bookId" value="${b.id}">
                                        <button class="btn btn-approve btn-xs" type="submit">通过</button>
                                    </form>
                                    <form action="${ctx}/admin" method="post" class="inline-form reject-form">
                                        <input type="hidden" name="action" value="reject">
                                        <input type="hidden" name="bookId" value="${b.id}">
                                        <input type="text" name="reason" maxlength="100" placeholder="驳回原因（可选）">
                                        <button class="btn btn-reject btn-xs" type="submit">驳回</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <section class="admin-section">
        <div class="section-head compact">
            <div>
                <h2>用户管理</h2>
                <p>新增、修改、删除用户；admin 为最高管理员，其他管理员不能删除或修改该账号。</p>
            </div>
        </div>

        <form action="${ctx}/admin" method="post" class="add-user-form">
            <input type="hidden" name="action" value="saveUser">
            <input type="hidden" name="id" value="0">
            <div class="field"><label>用户名</label><input class="input" name="username" maxlength="20" placeholder="3-20 位字母/数字/下划线" required></div>
            <div class="field"><label>昵称</label><input class="input" name="nickname" maxlength="20" placeholder="展示名称" required></div>
            <div class="field"><label>邮箱</label><input class="input" name="email" maxlength="100" placeholder="可选"></div>
            <div class="field"><label>密码</label><input class="input" type="password" name="password" maxlength="32" placeholder="6-32 位" required></div>
            <div class="field"><label>角色</label>
                <select class="input" name="role"><option value="USER">普通用户</option><option value="ADMIN">管理员</option></select>
            </div>
            <button class="btn btn-primary btn-sm" type="submit">新增用户</button>
        </form>

        <div class="table-wrap">
            <table class="data-table">
                <thead>
                <tr><th>ID</th><th>用户名</th><th>昵称</th><th>邮箱</th><th>角色</th><th>注册时间</th><th>操作</th></tr>
                </thead>
                <tbody>
                <c:forEach var="u" items="${users}">
                    <tr>
                        <td>${u.id}</td>
                        <td><c:out value="${u.username}"/><c:if test="${u.username eq 'admin'}"><span class="super-tag">最高</span></c:if></td>
                        <td>
                            <form id="userForm${u.id}" action="${ctx}/admin" method="post">
                                <input type="hidden" name="action" value="saveUser">
                                <input type="hidden" name="id" value="${u.id}">
                                <input type="hidden" name="username" value="${fn:escapeXml(u.username)}">
                            </form>
                            <input class="input input-sm" name="nickname" form="userForm${u.id}"
                                   value="${fn:escapeXml(u.nickname)}" maxlength="20" required
                                   ${u.username eq 'admin' and loginUser.username ne 'admin' ? 'disabled' : ''}>
                        </td>
                        <td><input class="input input-sm" name="email" form="userForm${u.id}"
                                   value="${fn:escapeXml(u.email)}" maxlength="100"
                                   ${u.username eq 'admin' and loginUser.username ne 'admin' ? 'disabled' : ''}></td>
                        <td>
                            <select class="input input-sm" name="role" form="userForm${u.id}"
                                    ${u.username eq 'admin' and loginUser.username ne 'admin' ? 'disabled' : ''}>
                                <option value="USER" ${u.admin ? '' : 'selected'}>普通用户</option>
                                <option value="ADMIN" ${u.admin ? 'selected' : ''}>管理员</option>
                            </select>
                        </td>
                        <td>${fn:substring(u.createdAt, 0, 10)}</td>
                        <td class="row-actions">
                            <c:choose>
                                <c:when test="${u.username eq 'admin' and loginUser.username ne 'admin'}">
                                    <span class="super-note">最高管理员账号，只有 admin 可修改</span>
                                </c:when>
                                <c:otherwise>
                                    <input class="input input-sm" type="password" name="password" form="userForm${u.id}"
                                           placeholder="新密码" maxlength="32">
                                    <button class="btn btn-ghost btn-xs" type="submit" form="userForm${u.id}">保存</button>
                                    <c:if test="${u.username ne 'admin' and u.id != loginUser.id}">
                                        <form action="${ctx}/admin" method="post" class="inline-form"
                                              data-confirm="确定删除用户「${fn:escapeXml(u.displayName)}」吗？">
                                            <input type="hidden" name="action" value="deleteUser">
                                            <input type="hidden" name="id" value="${u.id}">
                                            <button class="btn btn-danger-soft btn-xs" type="submit">删除</button>
                                        </form>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </section>
</main>

<footer class="site-footer">
    <div class="footer-inner">
        <p><span class="footer-brand">墨卷书阁</span> · 让每一本书都回到它的位置</p>
    </div>
</footer>
<script src="${ctx}/assets/js/app.js?v=16"></script>
</body>
</html>
