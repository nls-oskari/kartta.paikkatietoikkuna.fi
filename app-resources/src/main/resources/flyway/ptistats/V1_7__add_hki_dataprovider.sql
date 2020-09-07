
-- datasource for "PxWEB on City of Helsinki"
INSERT INTO oskari_statistical_datasource (locale, config, plugin)
VALUES ('{"fi":{"name":"Helsingin kaupunki"},"en":{"name":"City of Helsinki"}}"',
'{"url":"http://api.aluesarjat.fi/PXWeb/api/v1/fi/Helsingin%20seudun%20tilastot/P%C3%A4%C3%A4kaupunkiseutu%20alueittain","info":{"url":"http://api.aluesarjat.fi"},"regionKey":"Alue","ignoredVariables":["Alue"]}', 'PxWEB');

-- Add layers
INSERT INTO oskari_maplayer(type, url,
                    name, dataprovider_id,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'http://geoserver.hel.fi/geoserver/wms',
    'seutukartta:Seutu_suuralueet', (select id from oskari_dataprovider where locale LIKE '%Yhteistyöaineistot%'),
    '{ "en" : {
         "name":"Pääkaupunkiseudun suuraluejako"
       },"fi" : {
         "name":"Pääkaupunkiseudun suuraluejako"
    }}',
    '{"statistics":{"featuresUrl":"http://geoserver.hel.fi/geoserver/wfs","regionIdTag":"kokotun","nameIdTag":"nimi"}}', 80, 'EPSG:3067', '1.3.0');


INSERT INTO oskari_maplayer(type, url,
                    name, dataprovider_id,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'http://geoserver.hel.fi/geoserver/wms',
    'seutukartta:Seutu_pienalueet', (select id from oskari_dataprovider where locale LIKE '%Yhteistyöaineistot%'),
    '{ "en" : {
         "name":"Pääkaupunkiseudun pienaluejako"
       },"fi" : {
         "name":"Pääkaupunkiseudun pienaluejako"
    }}',
    '{"statistics":{"featuresUrl":"http://geoserver.hel.fi/geoserver/wfs","regionIdTag":"kokotun","nameIdTag":"nimi"}}', 80, 'EPSG:3067', '1.3.0');

-- Link layers to datasource
INSERT INTO
    oskari_statistical_datasource_regionsets(datasource_id, layer_id)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%Helsingin kaupunki%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'seutukartta:Seutu_suuralueet'));

INSERT INTO
    oskari_statistical_datasource_regionsets(datasource_id, layer_id)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%Helsingin kaupunki%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'seutukartta:Seutu_pienalueet'));