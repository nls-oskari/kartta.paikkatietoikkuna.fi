-- 'http://geoserver.hel.fi/geoserver/wfs' -> 'https://kartta.hel.fi/ws/geoserver/avoindata/wfs'
UPDATE oskari_maplayer
SET
    url = 'https://kartta.hel.fi/ws/geoserver/avoindata/wfs',
    name = 'avoindata:Seutukartta_aluejako_pienalue',
    attributes = '{"statistics":{"regionIdTag":"kokotun","nameIdTag":"nimi"}}'
WHERE type='statslayer' AND name = 'seutukartta:Seutu_pienalueet';

UPDATE oskari_maplayer
SET
    url = 'https://kartta.hel.fi/ws/geoserver/avoindata/wfs',
    name = 'avoindata:Seutukartta_aluejako_suuralue',
    attributes = '{"statistics":{"regionIdTag":"kokotun","nameIdTag":"nimi"}}'
WHERE type='statslayer' AND name = 'seutukartta:Seutu_suuralueet';

UPDATE oskari_maplayer
SET
    url = 'https://kartta.hel.fi/ws/geoserver/avoindata/wfs',
    name = 'avoindata:Seutukartta_aluejako_tilastoalue',
    attributes = '{"statistics":{"regionIdTag":"kokotun","nameIdTag":"nimi"}}'
WHERE type='statslayer' AND name = 'seutukartta:Seutu_tilastoalueet';

UPDATE oskari_maplayer
SET
    url = 'https://kartta.hel.fi/ws/geoserver/avoindata/wfs',
    name = 'avoindata:Piirijako_suurpiiri',
    attributes = '{"statistics":{"regionIdTag":"kokotun","nameIdTag":"nimi"}}'
WHERE type='statslayer' AND name = 'hel:Helsinki_suurpiirit';

UPDATE oskari_maplayer
SET
    url = 'https://kartta.hel.fi/ws/geoserver/avoindata/wfs',
    name = 'avoindata:Piirijako_peruspiiri',
    attributes = '{"statistics":{"regionIdTag":"kokotun","nameIdTag":"nimi"}}'
WHERE type='statslayer' AND name = 'hel:Helsinki_peruspiirit';

UPDATE oskari_maplayer
SET
    url = 'https://kartta.hel.fi/ws/geoserver/avoindata/wfs',
    name = 'avoindata:Piirijako_osaalue',
    attributes = '{"statistics":{"regionIdTag":"kokotun","nameIdTag":"nimi"}}'
WHERE type='statslayer' AND name = 'seutukartta:Helsinki_osa-alueet';


