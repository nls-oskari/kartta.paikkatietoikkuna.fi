-- http://geo.stat.fi/geoserver/tilastointialueet/ows?SERVICE=WFS&VERSION=1.1.0&REQUEST=DescribeFeatureType&TYPENAME=tilastointialueet:avi4500k
INSERT INTO oskari_maplayer(type, url,
                    name, groupId,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'http://geo.stat.fi/geoserver/wms',
    'tilastointialueet:avi4500k', (select id from oskari_layergroup where locale LIKE '%Yhteistyöaineistot%'),
    '{ "en" : {
         "name":"AVI-alueet"
       },"fi" : {
         "name":"AVI-alueet"
    }}',
    '{"statistics":{"featuresUrl":"http://geo.stat.fi/geoserver/tilastointialueet/ows","regionIdTag":"avi","nameIdTag":"nimi"}}', 80, 'EPSG:3067', '1.3.0');

INSERT INTO oskari_maplayer(type, url,
                    name, groupId,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'http://geo.stat.fi/geoserver/wms',
    'tilastointialueet:maakunta4500k', (select id from oskari_layergroup where locale LIKE '%Yhteistyöaineistot%'),
    '{ "en" : {
         "name":"Maakunnat"
       },"fi" : {
         "name":"Maakunnat"
    }}',
    '{"statistics":{"featuresUrl":"http://geo.stat.fi/geoserver/tilastointialueet/ows","regionIdTag":"maakunta","nameIdTag":"nimi"}}', 80, 'EPSG:3067', '1.3.0');

INSERT INTO oskari_maplayer(type, url,
                    name, groupId,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'http://geo.stat.fi/geoserver/wms',
    'tilastointialueet:ely4500k', (select id from oskari_layergroup where locale LIKE '%Yhteistyöaineistot%'),
    '{ "en" : {
         "name":"ELY-alueet"
       },"fi" : {
         "name":"ELY-alueet"
    }}',
    '{"statistics":{"featuresUrl":"http://geo.stat.fi/geoserver/tilastointialueet/ows","regionIdTag":"ely","nameIdTag":"nimi"}}', 80, 'EPSG:3067', '1.3.0');

-- link layers
INSERT INTO
    oskari_statistical_layer(datasource_id, layer_id, config)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%SotkaNET%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'tilastointialueet:avi4500k'),
    '{"regionType":"ALUEHALLINTOVIRASTO"}');

INSERT INTO
    oskari_statistical_layer(datasource_id, layer_id, config)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%SotkaNET%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'tilastointialueet:maakunta4500k'),
    '{"regionType":"MAAKUNTA"}');

INSERT INTO
    oskari_statistical_layer(datasource_id, layer_id, config)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%SotkaNET%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'tilastointialueet:ely4500k'),
    '{"regionType":"ELY-KESKUS"}');