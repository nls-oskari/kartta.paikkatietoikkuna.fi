-- Reset printout to default config
UPDATE portti_bundle set config = '{}' where name = 'printout';
UPDATE portti_view_bundle_seq set config = '{}' where bundle_id = (select id from portti_bundle where name = 'printout');