delete from portti_view_bundle_seq
where bundleinstance = 'printout'
and exists (select id from portti_view v where id = view_id and name = 'Geoportal Ol Cesium');