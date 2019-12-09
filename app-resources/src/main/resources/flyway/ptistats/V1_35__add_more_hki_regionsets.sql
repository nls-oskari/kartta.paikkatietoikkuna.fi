-- Add layers
INSERT INTO oskari_maplayer(type, url,
                    name, dataprovider_id,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'http://geoserver.hel.fi/geoserver/wms',
    'seutukartta:Seutu_tilastoalueet', (select id from oskari_maplayer_group where locale LIKE '%Yhteistyöaineistot%'),
    '{ "en" : {
         "name":"Pääkaupunkiseudun tilastoaluejako"
       },"fi" : {
         "name":"Pääkaupunkiseudun tilastoaluejako"
    }}',
    '{"statistics":{"featuresUrl":"http://geoserver.hel.fi/geoserver/wfs","regionIdTag":"kokotun","nameIdTag":"nimi"}}', 80, 'EPSG:3879', '1.3.0');

INSERT INTO oskari_maplayer(type, url,
                    name, dataprovider_id,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'http://geoserver.hel.fi/geoserver/wms',
    'seutukartta:Helsinki_osa-alueet', (select id from oskari_maplayer_group where locale LIKE '%Yhteistyöaineistot%'),
    '{ "en" : {
         "name":"Helsingin osa-alueet"
       },"fi" : {
         "name":"Helsingin osa-alueet"
    }}',
    '{"statistics":{"featuresUrl":"http://geoserver.hel.fi/geoserver/wfs","regionIdTag":"kokotun","nameIdTag":"nimi"}}', 80, 'EPSG:3879', '1.3.0');


INSERT INTO oskari_maplayer(type, url,
                    name, dataprovider_id,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'http://geoserver.hel.fi/geoserver/wms',
    'hel:Helsinki_peruspiirit', (select id from oskari_maplayer_group where locale LIKE '%Yhteistyöaineistot%'),
    '{ "en" : {
         "name":"Helsingin peruspiirit"
       },"fi" : {
         "name":"Helsingin peruspiirit"
    }}',
    '{"statistics":{"featuresUrl":"http://geoserver.hel.fi/geoserver/wfs","regionIdTag":"kokotun","nameIdTag":"nimi_fi"}}', 80, 'EPSG:3879', '1.3.0');


INSERT INTO oskari_maplayer(type, url,
                    name, dataprovider_id,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'http://geoserver.hel.fi/geoserver/wms',
    'hel:Helsinki_suurpiirit', (select id from oskari_maplayer_group where locale LIKE '%Yhteistyöaineistot%'),
    '{ "en" : {
         "name":"Helsingin suurpiirit"
       },"fi" : {
         "name":"Helsingin suurpiirit"
    }}',
    '{"statistics":{"featuresUrl":"http://geoserver.hel.fi/geoserver/wfs","regionIdTag":"aluekoodi","nameIdTag":"nimi_fi"}}', 80, 'EPSG:3879', '1.3.0');

-- Link layers to datasource
INSERT INTO
    oskari_statistical_layer(datasource_id, layer_id)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%Helsingin kaupunki%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'seutukartta:Seutu_tilastoalueet'));

INSERT INTO
    oskari_statistical_layer(datasource_id, layer_id)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%Helsingin kaupunki%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'seutukartta:Helsinki_osa-alueet'));

INSERT INTO
    oskari_statistical_layer(datasource_id, layer_id)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%Helsingin kaupunki%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'hel:Helsinki_peruspiirit'));

INSERT INTO
    oskari_statistical_layer(datasource_id, layer_id)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%Helsingin kaupunki%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'hel:Helsinki_suurpiirit'));
