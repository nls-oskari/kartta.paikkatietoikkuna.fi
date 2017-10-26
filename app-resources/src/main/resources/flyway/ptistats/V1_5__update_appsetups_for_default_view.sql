
  UPDATE portti_view_bundle_seq set config='{}', startup='{
    "bundlename": "statsgrid",
    "metadata": {
        "Import-Bundle": {
            "statsgrid": {
                "bundlePath": "/Oskari/packages/statistics/"
            }
        }
    }
  }' where view_id = 1 and bundle_id = (select id from portti_bundle where name = 'statsgrid');