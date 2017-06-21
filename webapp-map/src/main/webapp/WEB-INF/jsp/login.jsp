<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html>
<head>
    <title>${viewName}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
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
            body, html {
                margin: 0;
                padding: 0;
                background-color: #000;
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
                background: url(/static/img/background_image.jpg);
                background-size: cover;
                height: 100%;
                position: relative;
                padding-top: 100px;
            }

            .link-to-map {
                position: absolute;
                left: 15px;
                top: 27px;
                text-align: left;
            }

            .link-to-map a {
                font-size: 18px;
                color: #CCC;
            }

            .login-information {
                font-size: 18px;
                color: #CCC;
                max-width: 600px;
                margin: 0 auto 30px auto;
                padding: 15px 15px;
                background-color: rgba(0, 0, 0, 0.5);
                line-height: 1.3em;
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
                width: 80%;
                max-width: 300px;
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
                <input type="submit" class="primary" />
            </form>
    </div>
</div>

</body>
</html>
