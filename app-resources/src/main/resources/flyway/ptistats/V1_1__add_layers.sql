INSERT INTO oskari_maplayer(type, url,
                    name, groupId,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'http://geo.stat.fi/geoserver/wms',
    'tilastointialueet:kunta4500k_2017', (select id from oskari_layergroup where locale LIKE '%Yhteistyöaineistot%'),
    '{ "en" : {
         "name":"Municipalities"
       },"fi" : {
         "name":"Kunnat"
    }}',
    '{"statistics":{"featuresUrl":"http://geo.stat.fi/geoserver/tilastointialueet/ows","regionIdTag":"kunta","nameIdTag":"nimi"}}', 80, 'EPSG:3067', '1.3.0');


INSERT INTO oskari_maplayer(type, url,
                    name, groupId,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'http://geo.stat.fi/geoserver/wms',
    'tilastointialueet:seutukunta1000k', (select id from oskari_layergroup where locale LIKE '%Yhteistyöaineistot%'),
    '{ "en" : {
         "name":"Seutukunnat"
       },"fi" : {
         "name":"Seutukunnat"
    }}',
    '{"statistics":{"featuresUrl":"http://geo.stat.fi/geoserver/tilastointialueet/ows","regionIdTag":"seutukunta","nameIdTag":"nimi"}}', 80, 'EPSG:3067', '1.3.0');
