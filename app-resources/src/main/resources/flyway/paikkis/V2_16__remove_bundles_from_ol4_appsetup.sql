-- update ol4 version for development appsetup
DELETE FROM portti_view_bundle_seq
WHERE view_id = (select id from portti_view where name = 'Geoportal OL3')
and bundle_id = (select id from portti_bundle where name = 'analyse');

DELETE FROM portti_view_bundle_seq
WHERE view_id = (select id from portti_view where name = 'Geoportal OL3')
and bundle_id = (select id from portti_bundle where name = 'system-message');