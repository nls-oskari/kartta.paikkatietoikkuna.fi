-- Remove 3D views
delete from portti_view where page = 'index3D' and application = 'full-map_experimental';

-- Remove 3D link from the default and user views
delete from portti_view_bundle_seq where bundle_id = (select id from portti_bundle where name = 'demo-link');
delete from portti_bundle where name = 'demo-link';
