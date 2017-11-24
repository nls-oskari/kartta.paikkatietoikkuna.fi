-- update layer name
UPDATE oskari_maplayer SET srs_name='EPSG:3879' where type = 'statslayer' and name = 'seutukartta:Seutu_suuralueet';
UPDATE oskari_maplayer SET srs_name='EPSG:3879' where type = 'statslayer' and name = 'seutukartta:Seutu_pienalueet';