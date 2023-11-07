
UPDATE oskari_statistical_datasource_regionsets SET config = '{
"regionType":"kunta",
"valueProcessor": "fi.nls.oskari.control.statistics.plugins.pxweb.parser.PrefixedRegionsValueProcessor",
"statsRegionPrefix": "KU"
}'
 where datasource_id = (select id from oskari_statistical_datasource where locale like '%Tilastokeskus - Kuntien avainluvut 2022 aluejaolla%') AND config = '{"regionType":"kunta"}';
