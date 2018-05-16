UPDATE oskari_statistical_layer SET config = '{"regionType":"maakunta", "valueProcessor": "fi.nls.oskari.control.statistics.plugins.pxweb.parser.PrefixedRegionsValueProcessor", "statsRegionPrefix": "MK"}'
 where datasource_id = (select id from oskari_statistical_datasource where locale like '%Tilastokeskus%') AND LOWER(config) LIKE '%maakunta%';
