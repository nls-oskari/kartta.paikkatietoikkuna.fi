<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Paikkatietoikkuna</title>
    <!-- Bootstrap -->
    <link href="/static/css/bootstrap.min.css" rel="stylesheet">
    <link href="/static/css/style.css" rel="stylesheet">

</head>
<body>
<div class="container main">
    <div class="row margin hidden-xs">
    </div>
    <div class="row logo">
      <div class="col-md-6 col-md-offset-3">
        <img src="/static/resources/images/Paikkatietoikkuna_logo_rgb.svg" viewBox="0 0 100 100" class="logo_image">
      </div>
    </div>
    <div class="row margin hidden-xs">
    </div>
    <div class="row link-panels">
        <div class="col-md-5 col-md-offset-1">
            <div class="panel map-panel">
                <div class="panel-body">
                    <p class="map-desc"><spring:message code="landing.map.desc"/></p>
                    <a class="btn btn-default maplink-btn" href="https://kartta.paikkatietoikkuna.fi/?lang=${pageContext.response.locale.language}" role="button"><spring:message code="landing.map.link"/><span class="arrow">&rsaquo;</span></a>
                </div>
                <div class="rss-panel">
                    <div>
                        <img class="rss-image" src="/static/resources/images/rss.svg">
                        <div class="rss-header"><spring:message code="landing.feed.notifications"/></div>
                    </div>
                    <div>
                        <ul class="rss-list">
                            <c:forEach var="item" items="${notifications}">
                                <li>
                                    <a class="rss-link" href="${item.link}">
                                        <div class="rss-item-date">
                                            <fmt:formatDate value="${item.pubDate}" pattern="d.M.yyyy"/>
                                        </div>
                                        ${item.title}
                                    </a>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-5">
            <div class="panel sdi-panel">
                <div class="panel-body">
                    <p><a class="btn btn-default paikkislink-btn" href="<spring:message code="landing.cms.current.link"/>" role="button"><spring:message code="landing.cms.current.label"/><span class="arrow">&rsaquo;</span></a></p>
                    <a class="datalink" href="<spring:message code="landing.cms.inspire.link"/>"><spring:message code="landing.cms.inspire.label"/></a>
                    <span class="arrow">&rsaquo;</span><br>
                    <a class="datalink" href="<spring:message code="landing.cms.positio.link"/>"><spring:message code="landing.cms.positio.label"/></a>
                    <span class="arrow">&rsaquo;</span><br>
                    <a class="datalink" href="<spring:message code="landing.cms.osallistu.link"/>"><spring:message code="landing.cms.osallistu.label"/></a>
                    <span class="arrow">&rsaquo;</span><br>
                    <a class="datalink" href="<spring:message code="landing.cms.ohjaava.link"/>"><spring:message code="landing.cms.ohjaava.label"/></a>
                    <span class="arrow">&rsaquo;</span><br>
                    <a class="datalink" href="<spring:message code="landing.cms.contact.link"/>"><spring:message code="landing.cms.contact.label"/></a>
                    <span class="arrow">&rsaquo;</span>
                </div>
                <div class="rss-panel">
                    <div>
                        <img class="rss-image" src="/static/resources/images/rss.svg">
                        <div class="rss-header"><spring:message code="landing.feed.news"/></div>
                    </div>
                    <div>
                        <ul class="rss-list">
                            <c:forEach var="item" items="${news}">
                                <li>
                                    <a class="rss-link" href="${item.link}">
                                        <div class="rss-item-date">
                                            <fmt:formatDate value="${item.pubDate}" pattern="d.M.yyyy"/>
                                        </div>
                                        ${item.title}
                                    </a>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <footer>
        <div class="row some-link">
            <a href="<spring:message code="landing.social.tw.link"/>">
                <img src="/static/resources/images/twitter-logo.svg" class="footer-image">
            </a>
            <a href="<spring:message code="landing.social.fb.link"/>">
                <img src="/static/resources/images/fb-logo.svg" class="footer-image">
            </a>
        </div>
        <div class="row footer-text">
            <p><spring:message code="landing.help"/></p>
        </div>

        <div class="row tos-link">
            <a href="<spring:message code="landing.tos.link"/>"><spring:message code="landing.tos.label"/></a>
        </div>

        <div class="row">
            <c:if test="${pageContext.response.locale.language != 'sv'}"><a href="/?lang=sv" class="language-link">På svenska</a></c:if>
            <c:if test="${pageContext.response.locale.language != 'en'}"><a href="/?lang=en" class="language-link">In English</a></c:if>
            <c:if test="${pageContext.response.locale.language != 'fi'}"><a href="/?lang=fi" class="language-link">Suomeksi</a></c:if>
        </div>
    </footer>
</div>
</body>
</html>