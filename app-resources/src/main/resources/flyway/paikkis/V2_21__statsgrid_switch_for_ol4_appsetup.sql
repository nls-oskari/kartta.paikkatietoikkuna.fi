
 UPDATE portti_view_bundle_seq SET startup = '{
    "bundlename" : "statsgrid",
    "metadata" : {
       "Import-Bundle" : {
          "statsgrid" : {
            "bundlePath" : "/Oskari/packages/statistics/"
          }
       }
    }
  }', config = '{}', state = '{}'
 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'statsgrid');
