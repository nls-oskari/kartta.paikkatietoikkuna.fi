
UPDATE oskari_statistical_datasource
SET locale = '{"fi":{"name":"Tilastokeskus - Kuntien avainluvut 2022 aluejaolla"},"sv":{"name":"Statistikcentralen - Kommunernas nyckeltal enligt områdesindelningen år 2022"},"en":{"name":"Statistics Finland - Municipal key figures with the 2022 regional division"}}',
config='{
	"url": "https://statfin.stat.fi/PxWeb/api/v1/{language}/Kuntien_avainluvut/uusin/142h.px",
	"info": {
		"url": "http://www.tilastokeskus.fi"
	},
	"metadataFile": "/tilastokeskus_pxweb_metadata.json",
	"regionKey": "Alue",
	"indicatorKey": "Tiedot",
  "hints" : {
    "dimensions" : [ {
      "id" : "Vuosi",
      "sort" : "DESC"
    }]
  }
}'
WHERE locale LIKE '%Tilastokeskus - Kuntien avainluvut 2021 aluejaolla%';