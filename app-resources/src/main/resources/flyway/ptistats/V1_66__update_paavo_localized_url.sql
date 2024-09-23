
UPDATE oskari_statistical_datasource
SET config='{
  	"url": "https://pxdata.stat.fi/pxweb/api/v1/{language}/Postinumeroalueittainen_avoin_tieto/uusin/paavo_pxt_12f7.px",
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
}'
WHERE locale LIKE '%Tilastokeskus - Paavo%';