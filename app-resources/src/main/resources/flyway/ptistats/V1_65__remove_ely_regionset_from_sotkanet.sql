DELETE FROM oskari_statistical_datasource_regionsets
where datasource_id = (SELECT id FROM oskari_statistical_datasource WHERE locale like '%Sotkanet%')
AND layer_id = (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'tilastointialueet:ely4500k');
