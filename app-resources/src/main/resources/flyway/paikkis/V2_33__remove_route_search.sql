delete from portti_view_bundle_seq where bundle_id = (select id from portti_bundle where name = 'routesearch');
delete from portti_bundle where name = 'routesearch';