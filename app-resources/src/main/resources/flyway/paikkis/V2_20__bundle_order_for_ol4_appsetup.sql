
UPDATE portti_view_bundle_seq SET seqno = 100 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'lang-overrides');

 UPDATE portti_view_bundle_seq SET seqno = 105 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'mapfull');

 UPDATE portti_view_bundle_seq SET seqno = 110 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'drawtools');

 UPDATE portti_view_bundle_seq SET seqno = 115 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'divmanazer');

 UPDATE portti_view_bundle_seq SET seqno = 120 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'toolbar');

 UPDATE portti_view_bundle_seq SET seqno = 125 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'statehandler');

 UPDATE portti_view_bundle_seq SET seqno = 130 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'infobox');

 UPDATE portti_view_bundle_seq SET seqno = 135 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'search');

 UPDATE portti_view_bundle_seq SET seqno = 140 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'layerselector2');

 UPDATE portti_view_bundle_seq SET seqno = 145 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'layerselection2');

 UPDATE portti_view_bundle_seq SET seqno = 150 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'maplegend');

 UPDATE portti_view_bundle_seq SET seqno = 155 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'publisher2');

 UPDATE portti_view_bundle_seq SET seqno = 160 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'statsgrid');

INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, startup) VALUES (
  (select id from portti_view where name = 'Geoportal OL3'),
  (select id from portti_bundle where name = 'analyse'),
  165,
  '{
    "bundlename" : "analyse",
    "metadata" : {
       "Import-Bundle" : {
          "analyse" : {
            "bundlePath" : "/Oskari/packages/analysis/ol3/"
          }
       }
    }
  }'
);
-- UPDATE portti_view_bundle_seq SET seqno = 165 where view_id = (select id from portti_view where name = 'Geoportal OL3')
-- AND bundle_id = (select id from portti_bundle where name = 'analyse');



 UPDATE portti_view_bundle_seq SET seqno = 170 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'metadataflyout');

 UPDATE portti_view_bundle_seq SET seqno = 175 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'routesearch');

 UPDATE portti_view_bundle_seq SET seqno = 180 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'userguide');

 UPDATE portti_view_bundle_seq SET seqno = 185 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'personaldata');

DELETE FROM portti_view_bundle_seq where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'myplaces2');
-- UPDATE portti_view_bundle_seq SET seqno = 190 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 --AND bundle_id = (select id from portti_bundle where name = 'myplaces2');

 UPDATE portti_view_bundle_seq SET seqno = 195 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'guidedtour');

 UPDATE portti_view_bundle_seq SET seqno = 205 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'backendstatus');

 UPDATE portti_view_bundle_seq SET seqno = 210 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'printout');

 UPDATE portti_view_bundle_seq SET seqno = 215 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'featuredata2');

 UPDATE portti_view_bundle_seq SET seqno = 220 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'metadatacatalogue');

 UPDATE portti_view_bundle_seq SET seqno = 225 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'myplacesimport');

 UPDATE portti_view_bundle_seq SET seqno = 230 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'findbycoordinates');

 UPDATE portti_view_bundle_seq SET seqno = 235 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'heatmap');

 UPDATE portti_view_bundle_seq SET seqno = 240 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'coordinatetool');

 UPDATE portti_view_bundle_seq SET seqno = 245 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'timeseries');

 UPDATE portti_view_bundle_seq SET seqno = 250 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'feedbackService');

 UPDATE portti_view_bundle_seq SET seqno = 255 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'maprotator');

 UPDATE portti_view_bundle_seq SET seqno = 260 where view_id = (select id from portti_view where name = 'Geoportal OL3')
 AND bundle_id = (select id from portti_bundle where name = 'register');

