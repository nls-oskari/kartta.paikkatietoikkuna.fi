UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "Seutukunnat"},"en": {"name": "Sub-regional unit"},"sv": {"name": "Ekonomisk region"}}'
WHERE name = 'tilastointialueet:seutukunta4500k';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "Kunnat"},"en": {"name": "Municipalities"},"sv": {"name": "Kommuner"}}'
WHERE name = 'tilastointialueet:kunta4500k';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "Pääkaupunkiseudun pienaluejako"},"en": {"name": "Helsinki Region Small Areas"}"sv": {"name": "Helsingforsregionens småområden"}}'
WHERE name = 'seutukartta:Seutu_pienalueet';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "Pääkaupunkiseudun suuraluejako"},"en": {"name": "Helsinki Region Major Areas"}"sv": {"name": "Helsingforsregionens storområden"}}'
WHERE name = 'seutukartta:Seutu_suuralueet';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "Maakunnat"},"en": {"name": "Regions"},"sv": {"name": "Landskap"}}'
WHERE name = 'tilastointialueet:maakunta4500k';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "ELY-alueet"},"en": {"name": "ELY-regions"},"sv": {"name": "NTM-områden"}}'
WHERE name = 'tilastointialueet:ely4500k';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "NUTS1-alueet"},"en": {"name": "NUTS1-regions"},"sv": {"name": "NUTS1-områden"}}'
WHERE name = 'dummy:nuts1';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "ERVA-alueet"},"en": {"name": "ERVA-regions"},"sv": {"name": "Specialupptagningsområden inom den högspecialiserade sjukvården"}}'
WHERE name = 'dummy:erva';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "Sairaanhoitopiirit"},"en": {"name": "Hospital districts"},"sv": {"name": "Sjukvårdsdistrikt"}}'
WHERE name = 'dummy:sairaanhoitopiiri';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "Pääkaupunkiseudun tilastoaluejako"},"en": {"name": "Helsinki Region Statistical Areas"},"sv": {"name": "Helsingforsregionens statistikområden"}}'
WHERE name = 'seutukartta:Seutu_tilastoalueet';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "Helsingin suurpiirit"},"en": {"name": "Helsinki major districts"},"sv": {"name": "Helsingfors stordistrikt"}}'
WHERE name = 'hel:Helsinki_suurpiirit';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "Helsingin osa-alueet"},"en": {"name": "Helsinki quarters"},"sv": {"name": "Helsingfors delområden"}}'
WHERE name = 'seutukartta:Helsinki_osa-alueet';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "Helsingin peruspiirit"},"en": {"name": "Helsinki districts"},"sv": {"name": "Helsingfors distrikt"}}'
WHERE name = 'hel:Helsinki_peruspiirit';

UPDATE oskari_maplayer
SET locale = '{"fi": {"name": "AVI-alueet"},"en": {"name": "AVI-regions"},"sv": {"name": "RFV-områden"}}'
WHERE name = 'tilastointialueet:avi4500k';