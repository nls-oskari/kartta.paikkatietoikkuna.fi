update oskari_statistical_datasource set config = '{
    "url": "https://pxdata.stat.fi/pxweb/api/v1/{language}/Postinumeroalueittainen_avoin_tieto/uusin/12f7.px",
    "info": {
        "url": "http://www.tilastokeskus.fi"
    },
    "regionKey": "postinumeroalue_4_20260101",
    "indicatorKey": "contentscode",
    "hints": {
        "dimensions": [{
            "id": "timeperiod_y",
            "sort": "DESC"
        }]
    }
}'
WHERE locale LIKE '%Tilastokeskus - Paavo%';

update oskari_statistical_datasource set config = '{
	"url": "https://statfin.stat.fi/PxWeb/api/v1/{language}/Kuntien_avainluvut/uusin/142h.px",
	"info": {
		"url": "http://www.tilastokeskus.fi"
	},
	"metadataFile": "/tilastokeskus_pxweb_metadata.json",
	"regionKey": "alue_23_20250101",
	"indicatorKey": "contentscode",
    "hints" : {
        "dimensions" : [ {
            "id": "timeperiod_y",
            "sort": "DESC"
        }]
    }
}'
WHERE locale LIKE '%Tilastokeskus - Kuntien avainluvut%';