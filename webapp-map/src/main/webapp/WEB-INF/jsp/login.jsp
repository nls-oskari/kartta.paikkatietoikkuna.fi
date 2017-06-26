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
                overflow: hidden;
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

            .login-form {
                padding-top: 50px;
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
    <c:if test="${language == 'fi'}">
        <div id="language">
            <a href="./login?lang=sv">På svenska</a> -
            <a href="./login?lang=en">In English</a>
        </div>

        <div class="link-to-map">
            <a href="/">Takaisin karttaikkunaan</a>
        </div>

        <div class="login-information">
            <p>Paikkatietoikkuna on uudistunut 27.6.2017. Rekisteröityneiden käyttäjien tunnukset on siirretty uusittuun palveluun, mutta salasanat on nollattu. Käy tilaamassa uusi salasana <a href="https://omatili.maanmittauslaitos.fi/user/password">tästä</a>, jos olet kirjautumassa uuteen palveluun ensimmäistä kertaa. </p>
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
    </c:if>

    <c:if test="${language == 'sv'}">
        <div id="language">
            <a href="./login?lang=fi">Suomeksi</a> -
            <a href="./login?lang=en">In English</a>
        </div>

        <div class="link-to-map">
            <a href="/">Tillbaka till karttaikkunaan</a>
        </div>

        <div class="login-information">
            <p>Paikkatietoikkuna har förnyats den 27.6.2017. Registrerade användarnamn har överförts till den nya tjänsten, men lösenorden har nollställts. Beställ nytt lösenord <a href="https://omatili.maanmittauslaitos.fi/user/password">här</a>, om du loggar in till den nya tjänsten för första gången. </p>
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
            <a href="https://omatili.maanmittauslaitos.fi/user/password">Har du glömt lösenordet?</a>
        </div>
        <div id="register">
            <a href="https://omatili.maanmittauslaitos.fi/">Registrera</a>
        </div>

    </c:if>

    <c:if test="${language == 'en'}">
        <div id="language">
            <a href="./login?lang=fi">Suomeksi</a> -
            <a href="./login?lang=sv">På svenska</a>
        </div>

        <div class="link-to-map">
            <a href="/">Back to Karttaikkuna</a>
        </div>

        <div class="login-information">
            <p>Paikkatietoikkuna has been renewed on June 27th, 2017. The existing user accounts have been moved to the new service, but all passwords have been cleared. You can set a new password <a href="https://omatili.maanmittauslaitos.fi/user/password">here</a>, if you are entering the new service for the first time. </p>
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
            <a href="https://omatili.maanmittauslaitos.fi/user/password">Forgot password?</a>
        </div>
        <div id="register">
            <a href="https://omatili.maanmittauslaitos.fi/">Register</a>
        </div>

    </c:if>


</div>

</body>
</html>
