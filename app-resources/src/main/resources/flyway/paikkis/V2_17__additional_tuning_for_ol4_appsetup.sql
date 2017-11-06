-- remove routingService as its not part of the current pti appsetup
DELETE FROM portti_view_bundle_seq
WHERE view_id = (select id from portti_view where name = 'Geoportal OL3')
and bundle_id = (select id from portti_bundle where name = 'routingService');

-- prepend pti language overrides to appsetup
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance)
VALUES (
  (select id from portti_view where name = 'Geoportal OL3'),
  (SELECT id FROM portti_bundle WHERE name='lang-overrides'),
  (SELECT min(seqno)-1 FROM portti_view_bundle_seq WHERE view_id=(select id from portti_view where name = 'Geoportal OL3')),
  (SELECT config FROM portti_bundle WHERE name='lang-overrides'),
  (SELECT state FROM portti_bundle WHERE name='lang-overrides'),
  (SELECT startup FROM portti_bundle WHERE name='lang-overrides'),
  'lang-overrides'
);

-- add register bundle
INSERT INTO portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, startup, bundleinstance)
VALUES (
  (select id from portti_view where name = 'Geoportal OL3'),
  (SELECT id FROM portti_bundle WHERE name='register'),
  (SELECT max(seqno)+1 FROM portti_view_bundle_seq WHERE view_id=(select id from portti_view where name = 'Geoportal OL3')),
  (SELECT config FROM portti_bundle WHERE name='register'),
  (SELECT state FROM portti_bundle WHERE name='register'),
  (SELECT startup FROM portti_bundle WHERE name='register'),
  'register'
);

-- update heatmap bundle path to ol4 version
UPDATE portti_view_bundle_seq SET startup = '{
    "bundlename" : "heatmap",
    "metadata" : {
        "Import-Bundle" : {
            "heatmap" : {
                "bundlePath" : "/Oskari/packages/mapping/ol3/"
            }
        }
    }
}' WHERE view_id = (select id from portti_view where name = 'Geoportal OL3')
and bundle_id = (select id from portti_bundle where name = 'heatmap');