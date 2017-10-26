
-- datasource for "SotkaNET"
INSERT INTO oskari_statistical_datasource (locale, config, plugin)
VALUES ('{"fi":{"name":"SotkaNET"},"sv":{"name":"SotkaNET"},"en":{"name":"SotkaNET"}}',
'{
  "url" : "http://www.sotkanet.fi/rest",
  "info" : {
    "url" : "http://www.sotkanet.fi"
  },
  "hints" : {
    "dimensions" : [ {
      "id" : "year",
      "sort" : "DESC"
    }, {
      "id" : "sex",
      "default" : "total"
    }]
  }
}', 'SotkaNET');

-- datasource for "MML Kauppahintarekisteri"
INSERT INTO oskari_statistical_datasource (locale, config, plugin)
VALUES ('{"fi":{"name":"KHR"},"sv":{"name":"KHR"},"en":{"name":"KHR"}}',
'{"url" : "http://khrwfste.nls.fi/kauppahintatilasto-rest"}', 'SotkaNET');


-- datasource for "PxWEB on Tilastokeskus"
INSERT INTO oskari_statistical_datasource (locale, config, plugin)
VALUES ('{"fi":{"name":"Tilastokeskus"},"en":{"name":"Tilastokeskus"}}"',
'{"url":"https://pxnet2.stat.fi/pxweb/api/v1/fi/Kuntien_avainluvut/2017/","info":{"url":"http://www.tilastokeskus.fi"},"regionKey":"Alue 2017","ignoredVariables":["Alue 2017"]}', 'PxWEB');

-- datasource for "omat indikaattorit"
INSERT INTO oskari_statistical_datasource (locale, config, plugin)
VALUES ('{"fi":{"name":"Omat indikaattorit"},"sv":{"name":"Dina indikatorer"},"en":{"name":"Your indicators"}}',
'{}', 'UserStats');