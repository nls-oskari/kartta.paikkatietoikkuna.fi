-- update ol4 version for development appsetup
UPDATE portti_view_bundle_seq SET startup = '{
    "bundlename" : "findbycoordinates",
    "metadata" : {
        "Import-Bundle" : {
            "findbycoordinates": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        }
    }
}' WHERE view_id = (select id from portti_view where name = 'Geoportal OL3')
and bundle_id = (select id from portti_bundle where name = 'findbycoordinates');