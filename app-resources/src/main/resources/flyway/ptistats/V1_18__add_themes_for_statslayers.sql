-- link all statslayers to Statistical units theme
INSERT INTO oskari_maplayer_group_link (maplayerid, groupid)
SELECT id as maplayerid, (select id from oskari_maplayer_group where locale like '%Statistical units%') as themeid FROM oskari_maplayer WHERE type='statslayer'
