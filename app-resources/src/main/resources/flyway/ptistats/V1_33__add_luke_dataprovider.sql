
-- datasource for "PxWEB on LUKE"
INSERT INTO oskari_statistical_datasource (locale, config, plugin)
VALUES ('{"fi":{"name":"Luonnonvarakeskus"},"en":{"name":"Natural Resources Institute Finland"},"sv":{"name":"Naturresursinstitutet"}}"',
'{
    "url":"https://statdb.luke.fi/pxweb/api/v1/fi/LUKE/02%20Maatalous/02%20Rakenne/02%20Maatalous-%20ja%20puutarhayritysten%20rakenne",
    "info":{"url":"https://stat.luke.fi/"},
    "regionKey":"ELY-keskus",
    "ignoredVariables":["ELY-keskus"],
    "indicatorKey": "Muuttuja"
}', 'PxWEB');

-- Link layers to datasource
INSERT INTO
    oskari_statistical_datasource_regionsets(datasource_id, layer_id)
VALUES(
    (SELECT id FROM oskari_statistical_datasource
        WHERE locale like '%Luonnonvarakeskus%'),
    (SELECT id FROM oskari_maplayer WHERE type='statslayer' AND name = 'tilastointialueet:ely4500k'));
