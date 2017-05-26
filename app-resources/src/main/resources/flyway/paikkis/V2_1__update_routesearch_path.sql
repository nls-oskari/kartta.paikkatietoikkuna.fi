UPDATE portti_bundle SET startup= '{
    "bundlename" : "routesearch",
    "metadata" : {
        "Import-Bundle" : {
            "routesearch" : {
                "bundlePath" : "/Oskari/packages/paikkatietoikkuna/bundle/"
            }
        }
    }
}' where name = 'routesearch';

UPDATE portti_view_bundle_seq SET startup= '{
    "bundlename" : "routesearch",
    "metadata" : {
        "Import-Bundle" : {
            "routesearch" : {
                "bundlePath" : "/Oskari/packages/paikkatietoikkuna/bundle/"
            }
        }
    }
}' where bundle_id = (select id from portti_bundle where name = 'routesearch');