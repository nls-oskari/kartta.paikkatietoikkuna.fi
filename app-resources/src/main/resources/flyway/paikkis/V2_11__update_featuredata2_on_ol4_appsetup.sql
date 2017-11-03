-- update ol4 version for development appsetup
UPDATE portti_view_bundle_seq SET startup = '{
    "bundlename" : "featuredata2",
    "metadata" : {
        "Import-Bundle" : {
            "featuredata2" : {
                "bundlePath" : "/Oskari/packages/framework/"
            }
        }
    }
}' WHERE view_id = (select id from portti_view where name = 'Geoportal OL3')
and bundle_id = (select id from portti_bundle where name = 'featuredata2');