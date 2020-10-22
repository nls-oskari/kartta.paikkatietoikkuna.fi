
-- Update API url for "PxWEB on City of Helsinki"
UPDATE oskari_statistical_datasource
SET config = '{"url":"https://api.aluesarjat.fi/api/v1/fi/Helsingin%20seudun%20tilastot/P%C3%A4%C3%A4kaupunkiseutu%20alueittain","info":{"url":"https://api.aluesarjat.fi"},"regionKey":"Alue","ignoredVariables":["Alue"]}'
WHERE locale LIKE '%City of Helsinki%';
