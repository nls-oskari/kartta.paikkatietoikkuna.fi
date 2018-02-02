-- remove "baselayer" for old statsgrid
delete from oskari_maplayer where type = 'statslayer' and id = 519;
