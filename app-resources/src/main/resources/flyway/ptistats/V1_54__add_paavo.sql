
-- datasource for "PxWEB on Tilastokeskus"
INSERT INTO oskari_statistical_datasource (locale, config, plugin)
VALUES ('{"fi":{"name":"Tilastokeskus - Postinumero aluejaolla"},"sv":{"name":"Statistikcentralen - Postområden"},"en":{"name":"Statistics Finland - Postal areas"}}',
'{
  	"url": "https://pxdata.stat.fi/pxweb/api/v1/fi/Postinumeroalueittainen_avoin_tieto/uusin/paavo_pxt_12f7.px",
  	"info": {
  		"url": "http://www.tilastokeskus.fi"
  	},
  	"regionKey": "Postinumeroalue",
  	"indicatorKey": "Tiedot",
     "hints": {
       "dimensions": [{
         "id" : "Vuosi",
         "sort" : "DESC"
       }]
     }
}', 'PxWEB');

-- link regionset layer
INSERT INTO
    oskari_statistical_datasource_regionsets(datasource_id, layer_id)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE config like '%Postinumeroalueittainen_avoin_tieto%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'postialue:pno'));
