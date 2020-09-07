-- http://geo.stat.fi/geoserver/tilastointialueet/ows?SERVICE=WFS&VERSION=1.1.0&REQUEST=DescribeFeatureType&TYPENAME=tilastointialueet:avi4500k
INSERT INTO oskari_maplayer(type, url,
                    name, dataprovider_id,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'http://dummy.url.to.be.replaced',
    'dummy:nuts1', (select id from oskari_dataprovider where locale LIKE '%Yhteistyöaineistot%'),
    '{ "en" : {
         "name":"NUTS1-regions"
       },"fi" : {
         "name":"NUTS1-alueet"
    }}',
    '{"statistics":{"featuresUrl":"http://dummy.url.to.be.replaced","regionIdTag":"nuts","nameIdTag":"nimi"}}', 80, 'EPSG:3067', '1.3.0');

INSERT INTO oskari_maplayer(type, url,
                    name, dataprovider_id,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'http://dummy.url.to.be.replaced',
    'dummy:sairaanhoitopiiri', (select id from oskari_dataprovider where locale LIKE '%Yhteistyöaineistot%'),
    '{ "en" : {
         "name":"Sairaanhoitopiiri"
       },"fi" : {
         "name":"Sairaanhoitopiiri"
    }}',
    '{"statistics":{"featuresUrl":"http://dummy.url.to.be.replaced","regionIdTag":"sairaanhoitopiiri","nameIdTag":"nimi"}}', 80, 'EPSG:3067', '1.3.0');

INSERT INTO oskari_maplayer(type, url,
                    name, dataprovider_id,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'http://dummy.url.to.be.replaced',
    'dummy:erva', (select id from oskari_dataprovider where locale LIKE '%Yhteistyöaineistot%'),
    '{ "en" : {
         "name":"ERVA-regions"
       },"fi" : {
         "name":"ERVA-alueet"
    }}',
    '{"statistics":{"featuresUrl":"http://dummy.url.to.be.replaced","regionIdTag":"erva","nameIdTag":"nimi"}}', 80, 'EPSG:3067', '1.3.0');

-- link layers
INSERT INTO
    oskari_statistical_datasource_regionsets(datasource_id, layer_id, config)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%SotkaNET%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'dummy:nuts1'),
    '{"regionType":"NUTS1"}');

INSERT INTO
    oskari_statistical_datasource_regionsets(datasource_id, layer_id, config)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%SotkaNET%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'dummy:sairaanhoitopiiri'),
    '{"regionType":"SAIRAANHOITOPIIRI"}');

INSERT INTO
    oskari_statistical_datasource_regionsets(datasource_id, layer_id, config)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%SotkaNET%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'dummy:erva'),
    '{"regionType":"ERVA"}');