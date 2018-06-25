UPDATE oskari_statistical_datasource SET config='{
	"url": "https://pxnet2.stat.fi/pxweb/api/v1/fi/Kuntien_avainluvut/2018/kuntien_avainluvut_2018_aikasarja.px",
	"info": {
		"url": "http://www.tilastokeskus.fi"
	},
	"regionKey": "Alue 2018",
	"indicatorKey": "Tiedot"
}' where locale LIKE '%Tilastokeskus%';