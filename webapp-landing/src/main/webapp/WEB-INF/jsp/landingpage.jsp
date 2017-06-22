<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Paikkatietoikkuna</title>
    <link href="/static/css/style.css" rel="stylesheet">

    <!-- Bootstrap -->
    <link href="/static/css/bootstrap.min.css" rel="stylesheet">

</head>
<body>
<div class="container main">
    <div class="row logo">
        <img src="/static/resources/images/Paikkatietoikkuna_logo_rgb.svg" viewBox="0 0 100 100" class="logo_image">
    </div>
    <div class="row">
        <div class="col-md-12 main-text">
            <p><spring:message code="landing.basic.info"/></p>

            <p><spring:message code="landing.passwd.reset.info"/></p>
        </div>
    </div>
    <div class="row link-panels">
        <div class="col-md-6">
            <div class="panel map-panel">
                <div class="panel-body">
                    <p><spring:message code="landing.map.desc"/></p>
                    <p><a class="btn btn-default maplink-btn" href="https://kartta.paikkatietoikkuna.fi/?lang=${pageContext.response.locale.language}" role="button"><spring:message code="landing.map.link"/>  &rsaquo;</a></p>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="panel sdi-panel">
                <div class="panel-body">

                    <p><a class="btn btn-default paikkislink-btn" href="<spring:message code="landing.cms.current.link"/>" role="button"><spring:message code="landing.cms.current.label"/> &rsaquo;</a></p>
                    <a class="datalink" href="<spring:message code="landing.cms.inspire.link"/>"><spring:message code="landing.cms.inspire.label"/> &rsaquo;</a><br>
                    <a class="datalink" href="<spring:message code="landing.cms.osallistu.link"/>"><spring:message code="landing.cms.osallistu.label"/> &rsaquo;</a><br>
                    <a class="datalink" href="<spring:message code="landing.cms.ohjaava.link"/>"><spring:message code="landing.cms.ohjaava.label"/> &rsaquo;</a><br>
                    <a class="datalink" href="<spring:message code="landing.cms.contact.link"/>"><spring:message code="landing.cms.contact.label"/> &rsaquo;</a><br>

                </div>
            </div>
        </div>
    </div>
    <footer>
        <c:if test="${pageContext.response.locale.language != 'sv'}"><a href="/?lang=sv" class="language-link">På svenska</a></c:if>
        <c:if test="${pageContext.response.locale.language != 'en'}"><a href="/?lang=en" class="language-link">In English</a></c:if>
        <c:if test="${pageContext.response.locale.language != 'fi'}"><a href="/?lang=fi" class="language-link">Suomeksi</a></c:if>
    </footer>
</div>
</body>
</html>