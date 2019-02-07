UPDATE oskari_dataprovider SET locale = '{"fi":{"name":"Väylävirasto"},"sv":{"name":"Trafikledsverket"},"en":{"name":"Finnish Transport Infrastructure Agency"}}' WHERE locale ILIKE '%"Liikennevirasto"%';
UPDATE oskari_maplayer SET locale = replace(locale, '"Liikennevirasto"', '"Väylävirasto"') WHERE locale ILIKE '%"Liikennevirasto"%';
UPDATE oskari_maplayer SET locale = replace(locale, '"Trafikverket"', '"Trafikledsverket"') WHERE locale ILIKE '%"Trafikverket"%';
UPDATE oskari_maplayer SET locale = replace(locale, '"sv":{"name":"Farled","subtitle":"Finnish Transport Agency"}', '"sv":{"name":"Farled","subtitle":"Trafikledsverket"}') WHERE locale ILIKE '%"sv":{"name":"Farled","subtitle":"Finnish Transport Agency"}%';
UPDATE oskari_maplayer SET locale = replace(locale, '"Finnish Transport Agency"', '"Finnish Transport Infrastructure Agency"') WHERE locale ILIKE '%"Finnish Transport Agency"%';
