-- 'http://geoserver.hel.fi/geoserver/wfs' -> 'https://kartta.hel.fi/ws/geoserver/avoindata/wfs'
UPDATE oskari_maplayer
SET
    url = 'https://kartta.hel.fi/ws/geoserver/avoindata/wfs',
    name = 'avoindata:Seutukartta_aluejako_pienalue',
    attributes = '{"statistics":{"featuresUrl": "https://kartta.hel.fi/ws/geoserver/avoindata/wfs","regionIdTag":"kokotun","nameIdTag":"nimi"}}'
WHERE type='statslayer' AND name = 'seutukartta:Seutu_pienalueet';

UPDATE oskari_maplayer
SET
    url = 'https://kartta.hel.fi/ws/geoserver/avoindata/wfs',
    name = 'avoindata:Seutukartta_aluejako_suuralue',
    attributes = '{"statistics":{"featuresUrl": "https://kartta.hel.fi/ws/geoserver/avoindata/wfs","regionIdTag":"kokotun","nameIdTag":"nimi"}}'
WHERE type='statslayer' AND name = 'seutukartta:Seutu_suuralueet';

UPDATE oskari_maplayer
SET
    url = 'https://kartta.hel.fi/ws/geoserver/avoindata/wfs',
    name = 'avoindata:Seutukartta_aluejako_tilastoalue',
    attributes = '{"statistics":{"featuresUrl": "https://kartta.hel.fi/ws/geoserver/avoindata/wfs","regionIdTag":"kokotun","nameIdTag":"nimi"}}'
WHERE type='statslayer' AND name = 'seutukartta:Seutu_tilastoalueet';

