
-- remove if it's already linked (in dev envs etc)
DELETE FROM oskari_statistical_datasource_regionsets
where datasource_id = (SELECT id FROM oskari_statistical_datasource WHERE locale like '%Sotkanet%')
AND layer_id = (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'tilastointialueet:hyvinvointialue4500k');

-- add the link
INSERT INTO
    oskari_statistical_datasource_regionsets(datasource_id, layer_id, config)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%Sotkanet%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'tilastointialueet:hyvinvointialue4500k'),
    '{"regionType":"HYVINVOINTIALUE"}');
