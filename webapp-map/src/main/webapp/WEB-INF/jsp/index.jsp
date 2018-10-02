<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html>
<head>
    <title>${viewName}</title>
    <link rel="shortcut icon" href="/static/img/pti_icon.png" type="image/png" />
    <script type="text/javascript" src="/Oskari/libraries/jquery/jquery-3.3.1.min.js">
    </script>
    <!-- script src="https://code.jquery.com/jquery-migrate-3.0.1.js"></script -->

    <!-- ############# css ################# -->
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari/resources/css/forms.css"/>
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari/resources/css/portal.css"/>
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/icons.css"/>
    <link
            rel="stylesheet"
            type="text/css"
            href="/Oskari${path}/css/overwritten.css"/>
    <style type="text/css">
        @media screen {
            body {
                margin: 0;
                padding: 0;
            }

            #content {
                position: relative;
            }

            #sidebar {
                background-color: #FFDE00;
                width: 40px;
                position: fixed;
                display: block;
                z-index: 3;
                height: 100%;
                margin-left: -40px;
            }

            #pti-icon {
                height: 25px;
                margin-top: 25px;
                margin-left: 10px;
            }

            #pti-name {
                width: 135px;
                margin-top: 15px;
                margin-left: 15px;
                margin-bottom: 7px;
            }

            #mapContainer {
                display: block;
            }

            #mapdiv {
                width: 100%;
            }

            #maptools {
                background-color: #333438;
                height: 100%;
                position: absolute;
                top: 0;
                width: 153px;
                z-index: 2;
            }

            #contentMap {
                height: 100%;
                margin-left: 170px;
            }

            #oskari-system-messages {
                bottom: 1em;
                position: fixed;
                display: table;
                padding-left: 0.3em;
            }

            #language {
                padding: 0px 10px 0px 16px;
                color: #CCC;
            }

            #language a {
                color: #FFDE00;
                font-size: 12px;
                cursor: pointer;
                text-decoration: underline;
            }

        }
    </style>
    <!-- ############# /css ################# -->
</head>
<body class="mml-map">

<div id="wrapper">
    <div id="sidebar">
        <c:if test="${not empty language}">
            <c:set var="langParam" value="/?lang=${language}"/>
        </c:if>
        <a href="https://www.paikkatietoikkuna.fi${langParam}">
            <img id="pti-icon" src="/static/img/ikkuna.svg">
        </a>
    </div>
    <section id="content" class="floatleft">
        <nav id="maptools">
            <div id="logobar">
                <img id="pti-name" src="/static/img/paikkatietoikkuna_138px.svg">
            </div>
            <div id="menubar">
            </div>
            <div id="divider">
            </div>
            <div id="loginbar">
            </div>
            <div id="language">
                <c:if test="${language == 'fi'}">
                    <a href="./?lang=sv">På svenska</a> -
                    <a href="./?lang=en">In English</a>
                </c:if>
                <c:if test="${language == 'sv'}">
                    <a href="./?lang=fi">Suomeksi</a> -
                    <a href="./?lang=en">In English</a>
                </c:if>
                <c:if test="${language == 'en'}">
                    <a href="./?lang=fi">Suomeksi</a> -
                    <a href="./?lang=sv">På svenska</a>
                </c:if>
            </div>
            <div id="toolbar">
            </div>
            <div id="oskari-system-messages"></div>
        </nav>
        <div id="contentMap" class="oskariui container-fluid">
            <div id="menutoolbar" class="container-fluid"></div>
            <div class="row-fluid oskariui-mode-content" style="height: 100%; background-color:white;">
                <div class="oskariui-left"></div>
                <div class="span12 oskariui-center" style="height: 100%; margin: 0;">
                    <div id="mapdiv"></div>
                </div>
                <div class="oskari-closed oskariui-right">
                    <div id="mapdivB"></div>
                </div>
            </div>
        </div>
    </section>
</div>


<!-- ############# Javascript ################# -->

<!--  OSKARI -->

<script type="text/javascript">
    var ajaxUrl = '${ajaxUrl}';
    var controlParams = ${controlParams};
</script>

<c:if test="${preloaded}">
    <!-- Pre-compiled application JS, empty unless created by build job -->
    <script type="text/javascript"
            src="/Oskari${path}/oskari.min.js">
    </script>
    <%--language files --%>
    <script type="text/javascript"
            src="/Oskari${path}/oskari_lang_${language}.js">
    </script>
</c:if>

<script type="text/javascript"
        src="/Oskari${path}/index.js">
</script>


<!-- ############# /Javascript ################# -->
<jsp:useBean id="props" class="fi.nls.oskari.util.PropertyUtil"/>
<c:set var="ribbon" scope="page" value="${props.getOptional('page.ribbon')}" />
<c:if test="${!empty ribbon}">
    <style type="text/css">

        #demoribbon
        {
            color:white;
            background-color: red;
            z-index: 100000;
            top: 0px;
            left: 0px;
            position: fixed;
            <%--  left side --%>
            padding-left: 40px;
            margin-top: 20px;
            margin-left: -30px;
            padding-right: 40px;
            -webkit-transform: rotate(-45deg);
            -moz-transform: rotate(-45deg);
            transform: rotate(-45deg);
            <%--  right side --%>
            <%--
            margin-right: -30px;
            padding-left: 40px;
            padding-right: 40px;
            margin-top: 20px;
            -webkit-transform: rotate(45deg);
            -moz-transform: rotate(45deg);
            transform: rotate(45deg);
             --%>
        }
        #demoribbon:hover {
            display:none;
        }
    </style>
    <div id="demoribbon">${ribbon}</div>
</c:if>
</body>
</html>
