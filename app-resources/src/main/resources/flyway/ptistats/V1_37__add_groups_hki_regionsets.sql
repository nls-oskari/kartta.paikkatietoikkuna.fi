
INSERT INTO oskari_maplayer_group_link (maplayerid, groupid, order_number)
SELECT id as maplayerid, (select id from oskari_maplayer_group where locale like '%Statistical units%') as themeid, 1000011 as orderNum
 FROM oskari_maplayer WHERE type='statslayer' AND name = 'seutukartta:Seutu_tilastoalueet';


INSERT INTO oskari_maplayer_group_link (maplayerid, groupid, order_number)
SELECT id as maplayerid, (select id from oskari_maplayer_group where locale like '%Statistical units%') as themeid, 1000012 as orderNum
 FROM oskari_maplayer WHERE type='statslayer' AND name = 'seutukartta:Helsinki_osa-alueet';

INSERT INTO oskari_maplayer_group_link (maplayerid, groupid, order_number)
SELECT id as maplayerid, (select id from oskari_maplayer_group where locale like '%Statistical units%') as themeid, 1000013 as orderNum
 FROM oskari_maplayer WHERE type='statslayer' AND name = 'hel:Helsinki_peruspiirit';

INSERT INTO oskari_maplayer_group_link (maplayerid, groupid, order_number)
SELECT id as maplayerid, (select id from oskari_maplayer_group where locale like '%Statistical units%') as themeid, 1000013 as orderNum
 FROM oskari_maplayer WHERE type='statslayer' AND name = 'hel:Helsinki_suurpiirit';
