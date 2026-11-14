<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="code" value="${requestScope['javax.servlet.error.status_code']}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>出了点小问题 · 墨卷书阁</title>
    <link rel="stylesheet" href="${ctx}/assets/css/app.css?v=20">
</head>
<body class="error-page"><script>(function(){try{var t=localStorage.getItem("siteTheme")||localStorage.getItem("readerTheme");if(t==="green"||t==="black"){document.body.className+=(document.body.className?" ":"")+"theme-"+t;}}catch(e){}})();</script>
<div class="error-card">
    <span class="error-mark">卷</span>
    <p class="error-code">${empty code ? '500' : code}</p>
    <h1>这一页暂时找不到</h1>
    <p class="muted">可能是链接过期、内容被删除，或者服务器开小差了。</p>
    <div class="error-actions">
        <a class="btn btn-primary" href="${ctx}/home">回到首页</a>
        <a class="btn btn-ghost" href="${ctx}/login.jsp">重新登录</a>
    </div>
</div>
</body>
</html>
