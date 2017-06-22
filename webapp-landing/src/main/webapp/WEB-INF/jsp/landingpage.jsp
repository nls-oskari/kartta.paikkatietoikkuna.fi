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
            <p>Paikkatietoikkuna on uudistunut. Voit siirtyä karttapalveluun keltaisen laatikon kautta tai suoraan osoitteella <a href="https://kartta.paikkatietoikkuna.fi">https://kartta.paikkatietoikkuna.fi</a>. INSPIRE-tukisivut ja muut sisällöt löydät jatkossa Maanmittauslaitoksen verkkosivuilta <a href="http://maanmittauslaitos.fi/kartat-ja-paikkatieto/paikkatietojen-yhteiskayttö/ajankohtaista">Paikkatietojen yhteiskäyttö</a> -osiosta. Pääset niille myös alla olevista linkeistä.
            </p>

            <p>Rekisteröityneiden käyttäjien tunnukset on siirretty uuteen järjestelmään, mutta salasanat on nollattu. Voit tilata uuden salasanan <a href="https://omatili.maanmittauslaitos.fi/user/password">tästä</a>. Ongelmatilanteissa ota yhteyttä asiakaspalveluumme: paikkatietoikkuna@maanmittauslaitos.fi.</p>
        </div>
    </div>
    <div class="row link-panels">
        <div class="col-md-6">
            <div class="panel map-panel">
                <div class="panel-body">
                    <p>Karttapalvelussa voit esimerkiksi katsella eri organisaatioiden paikkatietoja, luoda teemakarttoja tai julkaista kartan omilla verkkosivuillasi.</p>
                    <p><a class="btn btn-default maplink-btn" href="#" role="button">Siirry karttaikkunaan &rsaquo;</a></p>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="panel sdi-panel">
                <div class="panel-body">
                    <p><a class="btn btn-default paikkislink-btn" href="#" role="button">Paikkatietojen yhteiskäyttö &rsaquo;</a></p>
                    <a class="datalink" href="#">INSPIRE &rsaquo;</a><br>
                    <a class="datalink" href="#">Mukaan toimintaan &rsaquo;</a><br>
                    <a class="datalink" href="#">Ohjaava toiminta &rsaquo;</a><br>
                    <a class="datalink" href="#">Yhteystiedot &rsaquo;</a><br>
                </div>
            </div>
        </div>
    </div>
    <footer>
        <a href="sv.html" class="language-link">På svenska</a>
        <a href="en.html" class="language-link">In English</a>
    </footer>
</div>
</body>
</html>