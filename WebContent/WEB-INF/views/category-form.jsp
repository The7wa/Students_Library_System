<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:choose>
    <c:when test="${category.id > 0}">
        <c:url var="cancelUrl" value="/category">
            <c:param name="id" value="${category.id}"/>
        </c:url>
    </c:when>
    <c:otherwise>
        <c:set var="cancelUrl" value="${ctx}/home"/>
    </c:otherwise>
</c:choose>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${mode eq 'edit' ? '编辑板块' : '新建板块'} · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css?v=19">
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
    <div class="form-page">
        <nav class="breadcrumb">
            <a href="${ctx}/home">首页</a>
            <span class="sep">/</span>
            <c:if test="${category.id > 0}">
                <a href="${ctx}/category?id=${category.id}">返回书架</a>
                <span class="sep">/</span>
            </c:if>
            <span>${mode eq 'edit' ? '编辑板块' : '新建板块'}</span>
        </nav>

        <section class="form-card">
            <div class="form-head">
                <span class="kicker">${mode eq 'edit' ? 'EDIT COLLECTION' : 'NEW COLLECTION'}</span>
                <h1>${mode eq 'edit' ? '编辑图书板块' : '新建图书板块'}</h1>
                <p>板块是书架的上一层分类。取一个清楚的名字，配一个图标和主题色，它会出现在首页。</p>
            </div>

            <form action="${ctx}/category" method="post" novalidate>
                <input type="hidden" name="action" value="save">
                <input type="hidden" name="id" value="${category.id}">

                <div class="form-grid">
                    <div class="field">
                        <label for="name">板块名称 <span class="req">*</span></label>
                        <input class="input ${not empty errors.name ? 'error' : ''}"
                               id="name" name="name" type="text" maxlength="30"
                               value="${fn:escapeXml(category.name)}"
                               placeholder="例如：计算机技术" autofocus>
                        <c:if test="${not empty errors.name}">
                            <span class="error-text"><c:out value="${errors.name}"/></span>
                        </c:if>
                    </div>

                    <div class="field">
                        <label for="sortOrder">排序权重 <span class="hint">数字越小越靠前</span></label>
                        <input class="input" id="sortOrder" name="sortOrder" type="number"
                               step="1" value="${category.sortOrder}" placeholder="0">
                    </div>

                    <div class="field field-full">
                        <label for="icon">板块图标 <span class="hint">填一个 emoji，或点下面的快捷图标</span></label>
                        <div class="emoji-field">
                            <input class="input ${not empty errors.icon ? 'error' : ''}"
                                   id="icon" name="icon" type="text" maxlength="8"
                                   value="${fn:escapeXml(category.icon)}">
                            <div class="emoji-presets" aria-label="快捷图标">
                                <button type="button" data-emoji="📚">📚</button>
                                <button type="button" data-emoji="📖">📖</button>
                                <button type="button" data-emoji="💻">💻</button>
                                <button type="button" data-emoji="🏛️">🏛️</button>
                                <button type="button" data-emoji="📊">📊</button>
                                <button type="button" data-emoji="🔬">🔬</button>
                                <button type="button" data-emoji="🎨">🎨</button>
                                <button type="button" data-emoji="🌍">🌍</button>
                                <button type="button" data-emoji="🧸">🧸</button>
                                <button type="button" data-emoji="☕">☕</button>
                                <button type="button" data-emoji="🎵">🎵</button>
                                <button type="button" data-emoji="🧠">🧠</button>
                            </div>
                        </div>
                        <c:if test="${not empty errors.icon}">
                            <span class="error-text"><c:out value="${errors.icon}"/></span>
                        </c:if>
                    </div>

                    <div class="field field-full">
                        <label for="description">一句话简介 <span class="hint">最多 120 字</span></label>
                        <input class="input ${not empty errors.description ? 'error' : ''}"
                               id="description" name="description" type="text" maxlength="120"
                               value="${fn:escapeXml(category.description)}"
                               placeholder="例如：代码、系统与工程实践">
                        <c:if test="${not empty errors.description}">
                            <span class="error-text"><c:out value="${errors.description}"/></span>
                        </c:if>
                    </div>

                    <div class="field field-full">
                        <label>主题色</label>
                        <div class="color-picker">
                            <label class="color-option cat-clay">
                                <input type="radio" name="color" value="clay" ${category.color eq 'clay' or empty category.color ? 'checked' : ''}>
                                <span class="swatch"></span>陶土
                            </label>
                            <label class="color-option cat-forest">
                                <input type="radio" name="color" value="forest" ${category.color eq 'forest' ? 'checked' : ''}>
                                <span class="swatch"></span>森林
                            </label>
                            <label class="color-option cat-ocean">
                                <input type="radio" name="color" value="ocean" ${category.color eq 'ocean' ? 'checked' : ''}>
                                <span class="swatch"></span>深海
                            </label>
                            <label class="color-option cat-plum">
                                <input type="radio" name="color" value="plum" ${category.color eq 'plum' ? 'checked' : ''}>
                                <span class="swatch"></span>梅紫
                            </label>
                            <label class="color-option cat-amber">
                                <input type="radio" name="color" value="amber" ${category.color eq 'amber' ? 'checked' : ''}>
                                <span class="swatch"></span>琥珀
                            </label>
                            <label class="color-option cat-slate">
                                <input type="radio" name="color" value="slate" ${category.color eq 'slate' ? 'checked' : ''}>
                                <span class="swatch"></span>石板
                            </label>
                            <label class="color-option cat-rose">
                                <input type="radio" name="color" value="rose" ${category.color eq 'rose' ? 'checked' : ''}>
                                <span class="swatch"></span>玫瑰
                            </label>
                            <label class="color-option cat-teal">
                                <input type="radio" name="color" value="teal" ${category.color eq 'teal' ? 'checked' : ''}>
                                <span class="swatch"></span>青碧
                            </label>
                        </div>
                    </div>
                </div>

                <div class="form-actions">
                    <span class="spacer">板块建好后，就可以往里面上架图书了</span>
                    <a class="btn btn-ghost" href="${cancelUrl}">取消</a>
                    <button type="submit" class="btn btn-primary">${mode eq 'edit' ? '保存修改' : '创建板块'}</button>
                </div>
            </form>
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
