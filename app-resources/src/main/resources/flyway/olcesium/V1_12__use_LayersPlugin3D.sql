UPDATE portti_view_bundle_seq s
SET config = replace(config, 'LayersPlugin', 'LayersPlugin3D')
where bundleinstance = 'mapfull'
and config not like '%LayersPlugin3D%'
and exists (select view_id from portti_view v where id = view_id and name = 'Geoportal Ol Cesium');