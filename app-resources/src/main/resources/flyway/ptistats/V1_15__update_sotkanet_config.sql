-- update layer name
UPDATE oskari_statistical_datasource set config='{
  "url" : "https://www.sotkanet.fi/rest",
  "info" : {
    "url" : "https://www.sotkanet.fi"
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
}' where locale like '%SotkaNET%';