<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html>
<head>
    <title>Paikkatietoikkuna</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="shortcut icon" href="/static/img/pti_icon.png" type="image/png" />
    <script type="text/javascript" src="/Oskari/libraries/jquery/jquery-1.10.2.min.js">
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
    <style type="text/css">
        @media screen {
            body, html {
                margin: 0;
                padding: 0;
                height: 100%;
                background-color: #000;
                background: url(/static/img/background_image.jpg);
                background-size: cover;
                background-attachment: fixed;
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

            #loginContainer {
                display: block;
                margin-left: 40px;
                text-align: center;
                height: calc(100% - 100px);
                position: relative;
                padding-top: 80px;
            }

            .link-to-map {
                position: absolute;
                left: 15px;
                top: 27px;
                text-align: left;
            }

            .link-to-map a {
                font-size: 18px;
            }

            /* 450px content height */
            .verticalSpacer {
                height: calc((100% - 450px) / 2);
            }

            .logo {
                text-align: center;
            }

            .logo > img {
                width: 80%;
                min-width: 400px;
                max-width: 555px;
            }

            .login-information {
                font-size: 18px;
                color: #CCC;
                max-width: 600px;
                margin: 50px auto 30px auto;
                padding: 15px 15px;
                background-color: rgba(0, 0, 0, 0.5);
                line-height: 1.3em;
            }

            input {
                margin: 10px;
                width: 80%;
                max-width: 300px;
            }

            #language {
                position:absolute;
                top: 27px;
                right: 15px;
            }

            a {
                color: #CCC;
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
    <div class="logo">
        <img src="/static/landingpage/resources/images/Paikkatietoikkuna_logo_rgb.svg" viewbox="0 0 100 100">
    </div>
    <div class="verticalSpacer"></div>
    <c:if test="${language == 'fi'}">
        <div id="language">
            <a href="./login?lang=sv">På svenska</a> -
            <a href="./login?lang=en">In English</a>
        </div>

        <div class="link-to-map">
            <a href="/">Karttapalveluun</a>
        </div>

        <div class="login-form">
            <c:if test="${error}">
                <b style="color:red;">Väärät kirjautumistiedot</b>
            </c:if>
            <form action="/my.policy" method="post">
                <input type="email" name="username" placeholder="Sähköpostiosoite" autofocus="autofocus"/><br/>
                <input type="password" name="password" placeholder="Salasana"/><br/>
                <input type="submit" class="primary" value="Kirjaudu"/>
            </form>
        </div>

        <div id="forgotPassword">
            <a href="https://omatili.maanmittauslaitos.fi/user/password">Unohditko salasanasi?</a>
        </div>
        <div id="register">
            <a href="https://omatili.maanmittauslaitos.fi/">Rekisteröidy</a>
        </div>

        <div class="login-information">
            <p>Ongelmatilanteissa ota yhteyttä asiakaspalveluumme: paikkatietoikkuna@maanmittauslaitos.fi.</p>
        </div>
    </c:if>

    <c:if test="${language == 'sv'}">
        <div id="language">
            <a href="./login?lang=fi">Suomeksi</a> -
            <a href="./login?lang=en">In English</a>
        </div>

        <div class="link-to-map">
            <a href="/">Till karttjänsten</a>
        </div>

        <div class="login-form">
            <c:if test="${error}">
                <b style="color:red;">Felaktiga uppgifter</b>
            </c:if>
            <form action="/my.policy" method="post">
                <input type="email" name="username" placeholder="E-postadress" autofocus="autofocus"/><br/>
                <input type="password" name="password" placeholder="Lösenord"/><br/>
                <input type="submit" class="primary" value="Logga in"/>
            </form>
        </div>


        <div id="forgotPassword">
            <a href="https://omatili.maanmittauslaitos.fi/user/password/?lang=sv">Har du glömt lösenordet?</a>
        </div>
        <div id="register">
            <a href="https://omatili.maanmittauslaitos.fi/?lang=sv">Registrera</a>
        </div>

        <div class="login-information">
            <p>Vid problem, kontakta vår kundservice paikkatietoikkuna@lantmateriverket.fi.</p>
        </div>
    </c:if>

    <c:if test="${language == 'en'}">
        <div id="language">
            <a href="./login?lang=fi">Suomeksi</a> -
            <a href="./login?lang=sv">På svenska</a>
        </div>

        <div class="link-to-map">
            <a href="/">To the map service</a>
        </div>

        <div class="login-form">
            <c:if test="${error}">
                <b style="color:red;">Wrong credentials</b>
            </c:if>
            <form action="/my.policy" method="post">
                <input type="email" name="username" placeholder="Email" autofocus="autofocus"/><br/>
                <input type="password" name="password" placeholder="Password"/><br/>
                <input type="submit" class="primary" value="Sign in"/>
            </form>
        </div>

        <div id="forgotPassword">
            <a href="https://omatili.maanmittauslaitos.fi/user/password/?lang=en">Forgot password?</a>
        </div>
        <div id="register">
            <a href="https://omatili.maanmittauslaitos.fi/?lang=en">Register</a>
        </div>

        <div class="login-information">
            <p>If you encounter problems on this site, please contact our customer service: paikkatietoikkuna@maanmittauslaitos.fi. </p>
        </div>
    </c:if>


</div>

</body>
</html>
