UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "Pääkaupunkiseudun pienaluejako"}, "en": {"name": "Helsinki Region Small Districts"}, "sv": {"name": "Huvudstadsregionens småområden"}}'
WHERE name = 'seutukartta:Seutu_pienalueet';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "Pääkaupunkiseudun suuraluejako"}, "en": {"name": "Helsinki Region Major Districts"}, "sv": {"name": "Huvudstadsregionens stordistrikten"}}'
WHERE name = 'seutukartta:Seutu_suuralueet';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "Pääkaupunkiseudun tilastoaluejako"}, "en": {"name": "Helsinki Region Statistical Districts"}, "sv": {"name": "Huvudstadsregionens statistiska områden"}}'
WHERE name = 'seutukartta:Seutu_tilastoalueet';
