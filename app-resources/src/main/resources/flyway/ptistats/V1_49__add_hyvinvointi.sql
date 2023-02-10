INSERT INTO oskari_maplayer(type, url,
                    name, dataprovider_id,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'https://geo.stat.fi/geoserver/ows',
    'tilastointialueet:hyvinvointialue4500k', (select id from oskari_dataprovider where locale LIKE '%Yhteistyöaineistot%'),
    '{
        "en": {
            "name": "Wellbeing services counties"
        },
        "fi": {
            "name": "Hyvinvointialueet"
        },
        "sv": {
            "name": "Välfärdsområden"
        }
    }',
    '{"statistics":{"featuresUrl":"https://geo.stat.fi/geoserver/ows","regionIdTag":"hyvinvointialue","nameIdTag":"nimi"}}', 80, 'EPSG:3067', '1.1.0');

INSERT INTO oskari_maplayer_group_link (maplayerid, groupid, order_number)
    SELECT id as maplayerid, (select id from oskari_maplayer_group where locale like '%Statistical units%') as themeid, 1000015 as orderNum
     FROM oskari_maplayer WHERE type='statslayer' AND name = 'tilastointialueet:hyvinvointialue4500k';

INSERT INTO
    oskari_statistical_datasource_regionsets(datasource_id, layer_id, config)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%Sotkanet%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'tilastointialueet:hyvinvointialue4500k'),
    '{"regionType":"HYVINVOINTIALUE"}');

INSERT INTO
    oskari_statistical_datasource_regionsets(datasource_id, layer_id)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%Omat indikaattorit%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'tilastointialueet:hyvinvointialue4500k'));
