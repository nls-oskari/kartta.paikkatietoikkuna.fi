
UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "Pääkaupunkiseudun pienaluejako"},"en": {"name": "Helsinki Region Small Areas"}, "sv": {"name": "Helsingforsregionens småområden"}}'
WHERE name = 'seutukartta:Seutu_pienalueet';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "Pääkaupunkiseudun suuraluejako"},"en": {"name": "Helsinki Region Major Areas"}, "sv": {"name": "Helsingforsregionens storområden"}}'
WHERE name = 'seutukartta:Seutu_suuralueet';
