-- Update opaskartta.turku.fi to https so they are not proxied
UPDATE oskari_maplayer
SET url = 'https' || substring(url from 5)
WHERE url LIKE 'http://opaskartta.turku.fi/%' AND type='wmslayer';

UPDATE oskari_resource
SET resource_mapping = 'wmslayer+https://' || substring(resource_mapping from 16)
  WHERE resource_mapping LIKE 'wmslayer+%opaskartta.turku.fi%';

DELETE FROM oskari_capabilities_cache WHERE url LIKE 'http://opaskartta.turku.fi%' AND layertype='wmslayer';