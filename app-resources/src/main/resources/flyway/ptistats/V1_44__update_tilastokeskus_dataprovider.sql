
UPDATE oskari_statistical_datasource
SET locale = '{"fi":{"name":"Tilastokeskus - Kuntien avainluvut 2020"},"sv":{"name":"Statistikcentralen - Kommunernas nyckeltal 2020"},"en":{"name":"Statistics Finland - Municipal key figures 2020"}}'
WHERE locale LIKE '%Tilastokeskus%';
