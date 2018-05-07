UPDATE oskari_statistical_layer SET config = '{"regionType":"seutukunta", "valueProcessor": "fi.nls.oskari.control.statistics.plugins.pxweb.parser.PrefixedRegionsValueProcessor", "statsRegionPrefix": "SK"}' 
 where datasource_id = (select id from oskari_statistical_datasource where locale like '%Tilastokeskus%') AND LOWER(config) LIKE '%seutukunta%';
   
INSERT INTO
    oskari_statistical_layer(datasource_id, layer_id, config)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%Tilastokeskus%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'tilastointialueet:maakunta4500k'),
    '{"regionType":"MAAKUNTA, "valueProcessor": "fi.nls.oskari.control.statistics.plugins.pxweb.parser.PrefixedRegionsValueProcessor", "statsRegionPrefix": "MK""}');
