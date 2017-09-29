<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html>
<html>
<head>
    <title>Paikkatietoikkuna</title>
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

            #loginContainer {
                display: block;
                margin-left: 40px;
                text-align: center;
                background: url(/static/img/background_image.jpg);
                background-size: cover;
                background-attachment: fixed;
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
            <a href="/">takaisin karttapalveluun</a>
        </div>

        <div class="login-information">
            <p>Rekisteröityneiden käyttäjien tunnukset on siirretty Maanmittauslaitoksen Omatili-palveluun, mutta salasanat on nollattu. Samat tunnukset käyvät tulevaisuudessa muihinkin verkkopalveluihin. Kun kirjaudut uusittuun palveluun ensimmäistä kertaa, toimi näin:<br><br>
            1) Tilaa uusi salasana <a href="https://omatili.maanmittauslaitos.fi/user/password">tästä</a> - saat sähköpostiisi ohjeet salasanan vaihtamiseksi.<br>
            2) Kun olet saanut salasanasi vaihdettua, päädyt Omatili-palvelun etusivulle. Palaa takaisin tälle sivulle, niin voit kirjautua Paikkatietoikkunaan uudella salasanallasi.</a></p><br>
            <p>Ongelmatilanteissa ota yhteyttä asiakaspalveluumme: paikkatietoikkuna@maanmittauslaitos.fi.</p>
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
            <a href="/">tillbaka till karttjänsten</a>
        </div>

        <div class="login-information">
            <p>Registrerade andvändarnamn har överförts till Lantmäteriverkets Omatili-tjänst, men lösenorden har nollställts. Samma inloggningsuppgifter kan användas i fortsättningen också i andra webbtjänster. När du loggar in i den nya tjänsten för första gången, gör så här:<br><br>
            1) Beställ ett nytt lösenord <a href="https://omatili.maanmittauslaitos.fi/user/password/?lang=sv">här</a> - du får instruktioner för bytet av lösenordet per e-post.<br>
            2) När du har lyckats byta lösenordet tas du vidare till Omatili-tjänstens framsida. Återvänd till denna sida för att logga in till Paikkatietoikkuna med ditt nya lösenord.</p><br>
            <p>Vid problem, kontakta vår kundservice paikkatietoikkuna@lantmateriverket.fi.</p>
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

    </c:if>

    <c:if test="${language == 'en'}">
        <div id="language">
            <a href="./login?lang=fi">Suomeksi</a> -
            <a href="./login?lang=sv">På svenska</a>
        </div>

        <div class="link-to-map">
            <a href="/">back to the map service</a>
        </div>

        <div class="login-information">
            <p>All existing user accounts have been moved to Omatili service provided by the National Land Survey of Finland. In the future, you can use the same account also in other web services. If you are entering the new service for the first time, please go through the following steps:<br><br>
            1) Order a new password <a href="https://omatili.maanmittauslaitos.fi/user/password/?lang=en">here</a> - you will receive instructions by e-mail.<br>
            2) After receiving the new password, you will be forwarded to the front page of Omatili service. Return to this page to log in to Paikkatietoikkuna.</p><br>
            <p>If you encounter problems on this site, please contact our customer service: paikkatietoikkuna@maanmittauslaitos.fi. </p>
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

    </c:if>


</div>

</body>
</html>
