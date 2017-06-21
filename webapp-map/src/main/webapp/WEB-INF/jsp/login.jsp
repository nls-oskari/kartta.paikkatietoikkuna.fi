<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html>
<head>
    <title>${viewName}</title>
    <link rel="shortcut icon" href="/static/img/pti_icon.png" type="image/png" />
    <script type="text/javascript" src="//code.jquery.com/jquery-1.7.2.min.js">
    </script>

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

            #sidebar {
                background-color: #FFDE00;
                width: 40px;
                position: fixed;
                display: block;
                z-index: 3;
                height: 100%;
            }

            #pti-icon {
                height: 25px;
                margin-top: 25px;
                margin-left: 10px;
            }

            #pti-name {
                margin-top: 15px;
                margin-right: 15px;
                margin-left: 15px;
                margin-bottom: 7px;
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

            #loginContainer {
                display: block;
                margin-left: 40px;
                text-align: center;
                padding-left: 150px;
                padding-right: 150px;
                background: url(/static/img/background_image.jpg);
                background-size: cover;
                height: 100%;
            }

            .link-to-map {
                padding-top: 40px;
                text-align: left;
            }

            .link-to-map a {
                font-size: 18px;
                color: #CCC;
            }

            .login-information {
                padding-top: 200px;
                font-size: 18px;
                padding-bottom: 50px;
                color: #CCC;
            }

            .login-information a {
                color: #CCC;
            }

            .login-form {
                padding-top: 50px;
            }

            p.login-title {
                font-size: 24px;
                color: #CCC;
                padding-bottom: 20px;
            }

            input {
                margin: 10px;
                width: 300px;
            }
        }
    </style>
    <!-- ############# /css ################# -->
</head>
<body>

<div id="sidebar">
    <img id="pti-icon" src="/static/img/ikkuna.svg">
</div>
<div id="loginContainer">
    <div class="link-to-map">
        <a href="/">Takaisin karttaikkunaan</a>
    </div>
    <div class="login-information">
        <p>Paikkatietoikkuna on uudistunut 27.6.2017. Rekisteröityneiden käyttäjien tunnukset on siirretty uusittuun palveluun, mutta salasanat on nollattu. Käy tilaamassa uusi salasana <a href="https://omatili.maanmittauslaitos.fi/user/password">tästä</a>, jos olet kirjautumassa uuteen palveluun ensimmäistä kertaa. </p>
    </div>
    <div class="login-form">
        <c:if test="${error}">
            <b style="color:red;">Wrong credentials</b>
        </c:if>
            <form action="/my.policy" method="post">
                <input type="email" name="username" placeholder="username" autofocus="autofocus"/><br/>
                <input type="password" name="password" placeholder="password"/><br/>
                <input type="submit" />
            </form>
    </div>
</div>

</body>
</html>
