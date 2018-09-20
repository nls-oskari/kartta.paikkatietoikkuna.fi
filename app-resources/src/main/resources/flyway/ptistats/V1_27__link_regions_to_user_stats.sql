INSERT INTO oskari_statistical_layer
SELECT ds.id ds_id, l.id layer_id, '{}' config
FROM oskari_statistical_datasource ds, oskari_maplayer l
WHERE ds.locale like '%Omat indikaattorit%' and l.type='statslayer'
and not exists (
    select sl.layer_id from oskari_statistical_layer sl
    where sl.datasource_id = ds.id and sl.layer_id = l.id
)