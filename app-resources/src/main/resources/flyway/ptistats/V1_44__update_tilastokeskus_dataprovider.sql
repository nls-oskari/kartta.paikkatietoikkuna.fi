
UPDATE oskari_statistical_datasource
SET locale = '{"fi":{"name":"Tilastokeskus - Kuntien avainluvut 2021 aluejaolla"},"sv":{"name":"Statistikcentralen - Kommunernas nyckeltal enligt områdesindelningen år 2021"},"en":{"name":"Statistics Finland - Municipal key figures with the 2021 regional division"}}',
config='{
	"url": "https://statfin.stat.fi/pxweb/api/v1/{language}/Kuntien_avainluvut/2021/kuntien_avainluvut_2021_aikasarja.px",
	"info": {
		"url": "http://www.tilastokeskus.fi"
	},
	"metadataFile": "/tilastokeskus_pxweb_metadata.json",
	"regionKey": "Alue 2021",
	"indicatorKey": "Tiedot",
  "hints" : {
    "dimensions" : [ {
      "id" : "Vuosi",
      "sort" : "DESC"
    }]
  }
}'
WHERE locale LIKE '%Tilastokeskus%';
