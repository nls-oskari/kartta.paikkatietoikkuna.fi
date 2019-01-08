UPDATE oskari_maplayer_group_link glink SET order_number = 1000001 FROM oskari_maplayer layer
WHERE layer.id = glink.maplayerid and layer.type = 'statslayer' and layer.locale like '%Pääkaupunkiseudun pienaluejako%';

UPDATE oskari_maplayer_group_link glink SET order_number = 1000002 FROM oskari_maplayer layer
WHERE layer.id = glink.maplayerid and layer.type = 'statslayer' and layer.locale like '%Pääkaupunkiseudun suuraluejako%';

UPDATE oskari_maplayer_group_link glink SET order_number = 1000003 FROM oskari_maplayer layer
WHERE layer.id = glink.maplayerid and layer.type = 'statslayer' and layer.locale like '%Kunnat%';

UPDATE oskari_maplayer_group_link glink SET order_number = 1000004 FROM oskari_maplayer layer
WHERE layer.id = glink.maplayerid and layer.type = 'statslayer' and layer.locale like '%Seutukunnat%';

UPDATE oskari_maplayer_group_link glink SET order_number = 1000005 FROM oskari_maplayer layer
WHERE layer.id = glink.maplayerid and layer.type = 'statslayer' and layer.locale like '%Sairaanhoitopiiri%';

UPDATE oskari_maplayer_group_link glink SET order_number = 1000006 FROM oskari_maplayer layer
WHERE layer.id = glink.maplayerid and layer.type = 'statslayer' and layer.locale like '%Maakunnat%';

UPDATE oskari_maplayer_group_link glink SET order_number = 1000007 FROM oskari_maplayer layer
WHERE layer.id = glink.maplayerid and layer.type = 'statslayer' and layer.locale like '%ELY-alueet%';

UPDATE oskari_maplayer_group_link glink SET order_number = 1000008 FROM oskari_maplayer layer
WHERE layer.id = glink.maplayerid and layer.type = 'statslayer' and layer.locale like '%AVI-alueet%';

UPDATE oskari_maplayer_group_link glink SET order_number = 1000009 FROM oskari_maplayer layer
WHERE layer.id = glink.maplayerid and layer.type = 'statslayer' and layer.locale like '%ERVA-alueet%';

UPDATE oskari_maplayer_group_link glink SET order_number = 1000010 FROM oskari_maplayer layer
WHERE layer.id = glink.maplayerid and layer.type = 'statslayer' and layer.locale like '%NUTS1-alueet%';