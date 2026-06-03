
UPDATE oskari_statistical_datasource
SET locale = '{"fi":{"name":"Tilastokeskus - Kuntien avainluvut 2026 aluejaolla"},"sv":{"name":"Statistikcentralen - Kommunernas nyckeltal enligt områdesindelningen år 2026"},"en":{"name":"Statistics Finland - Municipal key figures with the 2026 regional division"}}'
WHERE locale LIKE '%Tilastokeskus - Kuntien avainluvut 2025 aluejaolla%';
