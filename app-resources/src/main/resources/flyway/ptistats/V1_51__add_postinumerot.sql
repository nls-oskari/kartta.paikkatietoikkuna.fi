INSERT INTO oskari_maplayer(type, url,
                    name, dataprovider_id,
                    locale,
                    attributes, opacity, srs_name, version)
VALUES(
    'statslayer', 'https://geo.stat.fi/geoserver/ows',
    'postialue:pno', (select id from oskari_dataprovider where locale LIKE '%Yhteistyöaineistot%'),
    '{
        "en": {
            "name": "Postal code areas"
        },
        "fi": {
            "name": "Postinumeroalueet"
        },
        "sv": {
            "name": "Postnummerområden"
        }
    }',
    '{"statistics":{"featuresUrl":"https://geo.stat.fi/geoserver/ows","regionIdTag":"posti_alue","nameIdTag":"nimi"}}', 80, 'EPSG:3067', '1.1.0');

INSERT INTO oskari_maplayer_group_link (maplayerid, groupid, order_number)
    SELECT id as maplayerid, (select id from oskari_maplayer_group where locale like '%Statistical units%') as themeid, 1000015 as orderNum
     FROM oskari_maplayer WHERE type='statslayer' AND name = 'postialue:pno';

INSERT INTO
    oskari_statistical_datasource_regionsets(datasource_id, layer_id)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%Omat indikaattorit%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'postialue:pno'));
