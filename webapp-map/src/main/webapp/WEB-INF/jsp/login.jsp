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
            }
            body {
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
                height: calc(100% - 80px);
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

            .logo {
                text-align: center;
                margin-bottom: 15px;
            }

            .logo > img {
                width: 80%;
                min-width: 200px;
                max-width: 555px;
            }

            .login-information, .intro {
                font-size: 18px;
                color: #CCC;
                max-width: 600px;
                margin: 50px auto 30px auto;
                padding: 15px 15px;
                background-color: rgba(0, 0, 0, 0.5);
                line-height: 1.3em;
            }
            @media (max-width: 420px) {
                .login-information, .intro {
                    font-size: 15px;
                }
            }

            .verticalSpacer {
                /* content height 590px */
                height: calc((100% - 590px) * 0.3)
            }

            .intro {
                margin: 0 auto 30px auto;
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

            @media (max-width: 400px) {
                #sidebar, .intro {
                    display: none;
                }
                #loginContainer {
                    margin-left: 0;
                }
            }
            @media (min-width: 1000px) {
                #loginContainer {
                    height: calc(100% - 20px);
                    padding-top: 20px;
                }
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

        <div class="intro">
            <p>
                Kirjautuneena pääset julkaisemaan karttoja,
                käyttämään analyysitoimintoa sekä tallentamaan omia kohteita ja paikkatietoaineistoja.
            </p>
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
            <a href="https://omatili.maanmittauslaitos.fi/user/password/paikkatietoikkuna?lang=fi">Unohditko salasanasi?</a>
        </div>
        <div id="register">
            <a href="https://omatili.maanmittauslaitos.fi/user/new/paikkatietoikkuna?lang=fi">Rekisteröidy</a>
        </div>
        <div class="login-information">
            <div id="termsOfUse">
                Rekisteröitymällä hyväksyt Paikkatietoikkunan
                <a target="_blank" href="http://maanmittauslaitos.fi/asioi-verkossa/paikkatietoikkuna">käyttöehdot</a>
            </div>
            <div id="dataProtectionDescription">
                <a target="_blank" href="https://maanmittauslaitos.fi/tietoa-maanmittauslaitoksesta/organisaatio/tietosuojaselosteet/asiakassuhderekisteri">Asiakassuhderekisterin tietosuojaseloste</a>
            </div>
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

        <div class="intro">
            <p>
                Som inloggad användare kan du publicera inbäddade kartor, använda analysfuntionen
                samt spara egna platser och hämta egna datamängder till tjänsten.
            </p>
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
            <a href="https://omatili.maanmittauslaitos.fi/user/password/paikkatietoikkuna?lang=sv">Har du glömt lösenordet?</a>
        </div>
        <div id="register">
            <a href="https://omatili.maanmittauslaitos.fi/user/new/paikkatietoikkuna?lang=sv">Registrera</a>
        </div>

        <div class="login-information">
            <div id="termsOfUse">
                Genom att registrera dig som användare godkänner du
                <a target="_blank" href="http://maanmittauslaitos.fi/sv/e-tjanster/geodataportalen-paikkatietoikkuna">användarvillkoren</a>
            </div>
            <div id="dataProtectionDescription">
                <a target="_blank" href="https://maanmittauslaitos.fi/sv/information-om-lantmateriverket/organisation/dataskyddbeskrivningar/register-over-kundforhallanden">Dataskyddbeskrivningen för register över kundförhållanden</a>
            </div>
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

        <div class="intro">
            <p>
                As a logged-in user, you can publish embedded maps, use the analysis tool
                and save your own places or import your own dataset.
            </p>
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
            <a href="https://omatili.maanmittauslaitos.fi/user/password/paikkatietoikkuna?lang=en">Forgot password?</a>
        </div>
        <div id="register">
            <a href="https://omatili.maanmittauslaitos.fi/user/new/paikkatietoikkuna?lang=en">Register</a>
        </div>
        <div class="login-information">
            <div id="termsOfUse">
                By registering, you agree to the
                <a target="_blank" href="http://maanmittauslaitos.fi/en/e-services/geodata-portal-paikkatietoikkuna">Terms of Use of Paikkatietoikkuna</a>
            </div>
            <div id="dataProtectionDescription">
                <a target="_blank" href="https://maanmittauslaitos.fi/en/about-nls/organisation/data-protection-description/customer-relations-register">The data protection description of the customer relations register</a>
            </div>
        </div>

        <div class="login-information">
            <p>If you encounter problems on this site, please contact our customer service: paikkatietoikkuna@maanmittauslaitos.fi. </p>
        </div>
    </c:if>


</div>

</body>
</html>
