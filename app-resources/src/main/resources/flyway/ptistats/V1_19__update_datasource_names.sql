
UPDATE oskari_statistical_datasource set locale = '{"fi":{"name":"Helsingin kaupunki"},"sv":{"name":"Helsingfors stad"},"en":{"name":"City of Helsinki"}}' where config like '%http://api.aluesarjat.fi/PXWeb/api/v1/fi/Helsingin%';
UPDATE oskari_statistical_datasource set locale = '{"fi":{"name":"Sotkanet"},"sv":{"name":"Sotkanet"},"en":{"name":"Sotkanet"}}' where config like '%http://www.sotkanet.fi/rest%';
UPDATE oskari_statistical_datasource set locale = '{"name":"Kiinteistökauppojen tilastopalvelu"},"sv":{"name":"Köpeskillingsregistret över fastigheter"},"en":{"name":"Official purchase price register"}}' where config like '%https://khr.maanmittauslaitos.fi/tilastopalvelu/rest%';
UPDATE oskari_statistical_datasource set locale = '{"fi":{"name":"Tilastokeskus"},"sv":{"name":"Statistikcentralen"},"en":{"name":"Statistics Finland"}}' where config like '%https://pxnet2.stat.fi/pxweb/api/v1/fi/Kuntien_avainluvut%';
