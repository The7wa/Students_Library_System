<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="activeNav" value="home"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>首页 · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css?v=16?v=6">
</head>
<body><script>(function(){try{var t=localStorage.getItem("siteTheme")||localStorage.getItem("readerTheme");if(t==="green"||t==="black"){document.body.className+=(document.body.className?" ":"")+"theme-"+t;}}catch(e){}})();</script>
<header class="topbar">
    <div class="topbar-inner">
        <a class="brand" href="${ctx}/home" aria-label="墨卷书阁首页">
            <span class="brand-mark">墨</span>
            <span class="brand-text">
                <strong>墨卷书阁</strong>
                <small>LIBRARY SYSTEM</small>
            </span>
        </a>

        <form class="global-search" action="${ctx}/search" method="get" role="search">
            <svg viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="11" cy="11" r="7"></circle>
                <path d="M20 20l-3.2-3.2"></path>
            </svg>
            <input type="text" name="keyword" value="${fn:escapeXml(keyword)}"
                   placeholder="搜索书名、作者或出版社" autocomplete="off">
            <button type="submit">检索</button>
        </form>

        <nav class="topnav" aria-label="主导航">
            <a class="nav-link ${activeNav eq 'home' ? 'active' : ''}" href="${ctx}/home">首页</a>
            <a class="nav-link ${activeNav eq 'search' ? 'active' : ''}" href="${ctx}/search">全部图书</a>
            <a class="nav-link" href="${ctx}/book?action=myUploads">我的上传</a>
            <a class="nav-link" href="${ctx}/notification">通知<c:if test="${unreadNotifCount > 0}"><b class="notif-badge">${unreadNotifCount}</b></c:if></a>
            <c:if test="${loginUser.admin}">
                <a class="nav-link" href="${ctx}/admin">管理<c:if test="${pendingBookCount > 0}"><b class="notif-badge">${pendingBookCount}</b></c:if></a>
            </c:if>
            <span class="nav-divider"></span>
            <span class="user-chip" title="${fn:escapeXml(loginUser.displayName)}">
                <span class="avatar">${fn:substring(loginUser.displayName, 0, 1)}</span>
                <span class="user-meta">
                    <strong><c:out value="${loginUser.displayName}"/></strong>
                    <small>${loginUser.admin ? '管理员' : '读者'}</small>
                </span>
            </span>
            <a class="logout" href="${ctx}/auth?action=logout" title="退出登录">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                    <path d="M16 17l5-5-5-5M21 12H9"></path>
                </svg>
                退出
            </a>
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
    <section class="hero">
        <div class="hero-copy">
            <span class="eyebrow">WELCOME TO THE LIBRARY</span>
            <h1>${greeting}，<c:out value="${loginUser.displayName}"/>。<br>今天想从<em>哪个板块</em>开始？</h1>
            <p>书阁里共有 ${categoryCount} 个板块、${titleCount} 种在馆图书。点开一个板块，就能走进对应的书架，查找、上架或整理藏书。</p>
            <div class="hero-actions">
                <a class="btn btn-primary" href="#sections">浏览图书板块</a>
                <a class="btn btn-ghost" href="${ctx}/search">全馆检索</a>
            </div>
        </div>

        <div class="hero-stats" aria-label="馆藏统计">
            <div class="stat">
                <strong>${titleCount}</strong>
                <span>在馆书目</span>
            </div>
            <div class="stat">
                <strong>${readableCount}</strong>
                <span>可阅读</span>
            </div>
            <div class="stat">
                <strong>${categoryCount}</strong>
                <span>图书板块</span>
            </div>
            <div class="stat">
                <strong>${userCount}</strong>
                <span>注册读者</span>
            </div>
        </div>
    </section>

    <c:if test="${not empty latestBooks}">
        <section class="section recent-section" id="recent">
            <div class="section-head">
                <div>
                    <span class="kicker">NEW ARRIVALS</span>
                    <h2>最近上新</h2>
                    <p>刚刚收入书阁的三本书，点开封面就能开始阅读</p>
                </div>
                <a class="btn btn-soft btn-sm" href="${ctx}/search?sort=newest">查看全部 →</a>
            </div>
            <div class="recent-grid">
                <c:forEach var="b" items="${latestBooks}">
                    <a class="recent-card" href="${ctx}/book?action=detail&id=${b.id}">
                        <span class="recent-cover cat-${b.categoryColor}" data-cover-book="${b.id}">
                            <span class="cover-initial">${b.initial}</span>
                            <c:if test="${b.newArrival}">
                                <span class="cover-new">新<i>✦</i></span>
                            </c:if>
                            <span class="recent-cover-cat">${b.categoryIcon}</span>
                        </span>
                        <span class="recent-info">
                            <strong><c:out value="${b.name}"/></strong>
                            <small><c:out value="${b.author}"/> · <c:out value="${b.categoryName}"/></small>
                            <c:if test="${b.ratingCount > 0}">
                                <span class="card-stars">★ ${b.avgStarsLabel}</span>
                            </c:if>
                        </span>
                    </a>
                </c:forEach>
            </div>
        </section>
    </c:if>

    <section class="section" id="sections">
        <div class="section-head">
            <div>
                <span class="kicker">COLLECTIONS</span>
                <h2>图书板块</h2>
                <p>按主题分类的藏书空间，点击卡片进入对应书架</p>
            </div>
            <c:if test="${loginUser.admin}">
                <a class="btn btn-soft btn-sm" href="${ctx}/category?action=toAdd">＋ 新建板块</a>
            </c:if>
        </div>

        <div class="category-grid">
            <c:forEach var="cat" items="${categories}" varStatus="st">
                <article class="category-card cat-${cat.color}" style="--i:${st.index}">
                    <a class="category-link" href="${ctx}/category?id=${cat.id}">
                        <span class="cat-icon">${cat.icon}</span>
                        <span class="cat-count">${cat.bookCount} 本藏书</span>
                        <h3><c:out value="${cat.name}"/></h3>
                        <p><c:out value="${cat.description}"/></p>
                        <span class="cat-arrow">进入书架 →</span>
                    </a>
                    <c:if test="${loginUser.admin}">
                        <div class="cat-tools">
                            <a href="${ctx}/category?action=toEdit&id=${cat.id}">编辑</a>
                            <form action="${ctx}/category" method="post"
                                  data-confirm="删除板块「${fn:escapeXml(cat.name)}」会同时删除其中 ${cat.bookCount} 本图书，确定继续吗？">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="id" value="${cat.id}">
                                <button type="submit" class="link-danger">删除</button>
                            </form>
                        </div>
                    </c:if>
                </article>
            </c:forEach>

            <c:if test="${loginUser.admin}">
                <a class="add-category" href="${ctx}/category?action=toAdd">
                    <span class="plus">＋</span>
                    <strong>新建图书板块</strong>
                    <small>为下一类藏书留个位置</small>
                </a>
            </c:if>
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
