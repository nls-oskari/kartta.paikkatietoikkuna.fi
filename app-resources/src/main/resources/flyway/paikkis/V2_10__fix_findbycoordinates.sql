UPDATE portti_bundle SET startup = '{
    "title": "FindByCoordinates",
    "bundleinstancename": "findbycoordinates",
    "bundlename": "findbycoordinates",
    "metadata": {
        "Import-Bundle": {
            "findbycoordinates": {
                "bundlePath": "/Oskari/packages/framework/bundle/"
            }
        }
    }
}' WHERE name = 'findbycoordinates';