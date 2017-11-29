-- update layer name
UPDATE oskari_statistical_datasource set config='{
  "url" : "https://khr.maanmittauslaitos.fi/tilastopalvelu/rest",
  "info" : {
    "url" : "https://khr.maanmittauslaitos.fi/"
  },
  "hints" : {
    "dimensions" : [ {
      "id" : "year",
      "sort" : "DESC"
    }]
  }
}' where locale like '%KHR%';