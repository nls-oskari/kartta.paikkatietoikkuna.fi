-- update ui labels for Paavo
UPDATE oskari_statistical_datasource
SET locale = '{"fi":{"name":"Tilastokeskus - Paavo (Postinumeroalueittainen avoin tieto)"},"sv":{"name":"Statistikcentralen - Paavo (Öppen data efter postnummerområde)"},"en":{"name":"Statistics Finland - Paavo (Open data by postal code area)"}}'
WHERE config like '%Postinumeroalueittainen_avoin_tieto%';
