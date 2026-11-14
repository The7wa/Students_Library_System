<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:choose>
    <c:when test="${book.categoryId > 0}">
        <c:url var="cancelUrl" value="/category">
            <c:param name="id" value="${book.categoryId}"/>
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
    <title>${mode eq 'edit' ? '编辑图书' : '新增图书'} · 墨卷书阁</title>
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
            <c:if test="${book.categoryId > 0}">
                <a href="${ctx}/category?id=${book.categoryId}">返回书架</a>
                <span class="sep">/</span>
            </c:if>
            <span>${mode eq 'edit' ? '编辑图书' : '新增图书'}</span>
        </nav>

        <section class="form-card">
            <div class="form-head">
                <span class="kicker">${mode eq 'edit' ? 'EDIT BOOK' : 'NEW BOOK'}</span>
                <h1>${mode eq 'edit' ? '编辑图书信息' : (loginUser.admin ? '上架一本新书' : '上传一本新书')}</h1>
                <c:choose>
                    <c:when test="${loginUser.admin}">
                        <p>书名、作者与板块为必填项，正文 TXT 可以在上架后随时补充。</p>
                    </c:when>
                    <c:otherwise>
                        <p>提交后进入待审核状态，管理员通过后才会出现在书架上；你可以在“我的上传”里查看进度。</p>
                    </c:otherwise>
                </c:choose>
            </div>

            <form action="${ctx}/book" method="post" enctype="multipart/form-data" novalidate>
                <input type="hidden" name="action" value="save">
                <input type="hidden" name="id" value="${book.id}">

                <div class="form-grid">
                    <div class="field">
                        <label for="name">书名 <span class="req">*</span></label>
                        <input class="input ${not empty errors.name ? 'error' : ''}"
                               id="name" name="name" type="text" maxlength="100"
                               value="${fn:escapeXml(book.name)}" placeholder="例如：活着" autofocus>
                        <c:if test="${not empty errors.name}">
                            <span class="error-text"><c:out value="${errors.name}"/></span>
                        </c:if>
                    </div>

                    <div class="field">
                        <label for="author">作者 <span class="req">*</span></label>
                        <input class="input ${not empty errors.author ? 'error' : ''}"
                               id="author" name="author" type="text" maxlength="50"
                               value="${fn:escapeXml(book.author)}" placeholder="例如：余华">
                        <c:if test="${not empty errors.author}">
                            <span class="error-text"><c:out value="${errors.author}"/></span>
                        </c:if>
                    </div>

                    <div class="field">
                        <label for="categoryId">所属板块 <span class="req">*</span></label>
                        <select class="input ${not empty errors.categoryId ? 'error' : ''}"
                                id="categoryId" name="categoryId">
                            <option value="">请选择板块</option>
                            <c:forEach var="cat" items="${categories}">
                                <option value="${cat.id}" ${book.categoryId == cat.id ? 'selected' : ''}>
                                    ${cat.icon} <c:out value="${cat.name}"/>
                                </option>
                            </c:forEach>
                        </select>
                        <c:if test="${not empty errors.categoryId}">
                            <span class="error-text"><c:out value="${errors.categoryId}"/></span>
                        </c:if>
                    </div>

                    <div class="field field-full">
                        <label for="publisher">出版社 <span class="hint">选填</span></label>
                        <input class="input ${not empty errors.publisher ? 'error' : ''}"
                               id="publisher" name="publisher" type="text" maxlength="80"
                               value="${fn:escapeXml(book.publisher)}" placeholder="例如：作家出版社">
                        <c:if test="${not empty errors.publisher}">
                            <span class="error-text"><c:out value="${errors.publisher}"/></span>
                        </c:if>
                    </div>

                    <div class="field field-full">
                        <label for="contentFile">正文 TXT <span class="hint">选填，最大 20MB</span></label>

                        <c:if test="${not empty book.contentFile}">
                            <div class="current-file">
                                <span class="current-file-icon">📄</span>
                                <span class="current-file-text">
                                    已收录：
                                    <strong><c:out value="${not empty book.contentName ? book.contentName : book.contentFile}"/></strong>
                                </span>
                                <label class="check-inline">
                                    <input type="checkbox" name="removeContent" value="1"> 移除正文
                                </label>
                            </div>
                        </c:if>

                        <div class="file-drop">
                            <input class="file-input" id="contentFile" name="contentFile" type="file"
                                   accept=".txt,text/plain" data-file-input>
                            <label class="file-drop-body" for="contentFile">
                                <span class="file-drop-icon">＋</span>
                                <span class="file-drop-text">
                                    <strong>选择 TXT 文件</strong>
                                    <small>上传后即可在图书详情页在线阅读</small>
                                </span>
                            </label>
                            <span class="file-selected" data-file-name>未选择文件</span>
                        </div>

                        <div class="manual-file">
                            <label for="contentFileName">或使用正文目录中的文件 <span class="hint">选填</span></label>
                            <input class="input ${not empty errors.contentFileName ? 'error' : ''}"
                                   id="contentFileName" name="contentFileName" type="text" maxlength="160"
                                   value="" placeholder="例如：活着.txt">
                            <c:if test="${not empty errors.contentFileName}">
                                <span class="error-text"><c:out value="${errors.contentFileName}"/></span>
                            </c:if>
                        </div>

                        <c:if test="${not empty errors.contentFile}">
                            <span class="error-text"><c:out value="${errors.contentFile}"/></span>
                        </c:if>

                        <p class="field-note">
                            正文目录：<code><c:out value="${contentDir}"/></code><br>
                            也可以直接把下载好的 TXT 放进这个目录，再在上面的输入框里填写文件名。
                        </p>
                    </div>

                    <div class="field field-full">
                        <label for="description">内容简介 <span class="hint">选填，最多 2000 字</span></label>
                        <textarea class="textarea ${not empty errors.description ? 'error' : ''}"
                                  id="description" name="description" maxlength="2000"
                                  placeholder="用一两句话介绍这本书..."><c:out value="${book.description}"/></textarea>
                        <c:if test="${not empty errors.description}">
                            <span class="error-text"><c:out value="${errors.description}"/></span>
                        </c:if>
                    </div>
                </div>

                <div class="form-actions">
                    <span class="spacer">带 <span class="req">*</span> 的为必填项</span>
                    <a class="btn btn-ghost" href="${cancelUrl}">取消</a>
                    <button type="submit" class="btn btn-primary">${mode eq 'edit' ? '保存修改' : '确认上架'}</button>
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
