UPDATE oskari_maplayer
    SET minscale=-1, maxscale=-1
where
    internal = true
    and name in ('oskari:my_places', 'oskari:vuser_layer_data', 'oskari:analysis_data');
