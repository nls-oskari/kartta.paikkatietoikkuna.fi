
 UPDATE portti_view_bundle_seq SET startup = '{
	"metadata": {
		"Import-Bundle": {
			"maparcgis": {
				"bundlePath": "/Oskari/packages/mapping/ol3/"
			},
			"mapmodule": {
				"bundlePath": "/Oskari/packages/mapping/ol3/"
			},
			"oskariui": {
				"bundlePath": "/Oskari/packages/framework/bundle/"
			},
			"mapwfs2": {
				"bundlePath": "/Oskari/packages/mapping/ol3/"
			},
			"mapstats": {
				"bundlePath": "/Oskari/packages/mapping/ol3/"
			},
			"mapuserlayers": {
				"bundlePath": "/Oskari/packages/mapping/ol3/"
			},
			"ui-components": {
				"bundlePath": "/Oskari/packages/framework/bundle/"
			},
			"mapanalysis": {
				"bundlePath": "/Oskari/packages/mapping/ol3/"
			},
			"mapfull": {
				"bundlePath": "/Oskari/packages/framework/bundle/"
			},
			"mapwmts": {
				"bundlePath": "/Oskari/packages/mapping/ol3/"
			},
			"mapmyplaces": {
				"bundlePath": "/Oskari/packages/mapping/ol3/"
			}
		}
	},
	"bundlename": "mapfull"
}'
 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'mapfull');

INSERT INTO portti_bundle(name,startup) VALUES (
'myplaces3',
'{
    "bundlename" : "myplaces3",
    "metadata" : {
       "Import-Bundle" : {
          "myplaces3" : {
            "bundlePath" : "/Oskari/packages/framework/bundle/"
          }
       }
    }
  }'
);

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, startup) VALUES (
  (select id from portti_view where name = 'Geoportal OL3'),
  (select id from portti_bundle where name = 'myplaces3'),
  190,
  '{
    "bundlename" : "myplaces3",
    "metadata" : {
       "Import-Bundle" : {
          "myplaces3" : {
            "bundlePath" : "/Oskari/packages/framework/bundle/"
          }
       }
    }
  }'
);