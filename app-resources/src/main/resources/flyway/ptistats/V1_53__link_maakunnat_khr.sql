
INSERT INTO
    oskari_statistical_datasource_regionsets(datasource_id, layer_id, config)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%Kiinteistökauppojen tilastopalvelu%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'tilastointialueet:maakunta4500k'),
    '{"regionType":"maakunta"}');