
-- Update API url for "PxWEB on City of Helsinki"
UPDATE oskari_statistical_datasource
SET config = '{"url":"https://stat.hel.fi/api/v1/fi/Aluesarjat","info":{"url":"https://stat.hel.fi"},"regionKey":"Alue","ignoredVariables":["Alue"]}'
WHERE locale LIKE '%City of Helsinki%';
