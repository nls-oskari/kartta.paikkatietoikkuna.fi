UPDATE oskari_statistical_datasource SET config='{
	"url": "https://pxnet2.stat.fi/pxweb/api/v1/{language}/Kuntien_avainluvut/2020/kuntien_avainluvut_2020_aikasarja.px",
	"info": {
		"url": "http://www.tilastokeskus.fi"
	},
	"metadataFile": "/tilastokeskus_pxweb_metadata.json",
	"regionKey": "Alue 2020",
	"indicatorKey": "Tiedot",
  "hints" : {
    "dimensions" : [ {
      "id" : "Vuosi",
      "sort" : "DESC"
    }]
  }
}' WHERE locale LIKE '%Tilastokeskus%';
