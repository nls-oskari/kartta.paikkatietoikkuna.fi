insert into portti_view_bundle_seq (view_id, bundle_id, seqno, config, state, bundleinstance)
values (
(select id from portti_view where name = 'Geoportal Ol Cesium' limit 1),
(select id from portti_bundle where name = 'featuredata2' limit 1),
(select max(seqno) + 1 from portti_view_bundle_seq
    where view_id = (select id from portti_view where name = 'Geoportal Ol Cesium' limit 1)
    and seqno < 100
),
'{
   "selectionTools": true,
   "singleSelection" : true
 }',
 '{}',
'featuredata2'
);
