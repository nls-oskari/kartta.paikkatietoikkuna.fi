-- Update nls layers to https so they are not proxied
UPDATE oskari_maplayer
SET url = 'https' || substring(url from 5)
WHERE url LIKE 'http://karttamoottori.maanmittauslaitos.fi%' AND type='wmtslayer';

UPDATE oskari_resource
SET resource_mapping = 'wmtslayer+https' || substring(resource_mapping from 15)
  WHERE resource_mapping LIKE 'wmtslayer+http://karttamoottori.maanmittauslaitos.fi%';

DELETE FROM oskari_capabilities_cache WHERE url LIKE 'http://karttamoottori.maanmittauslaitos.fi%' AND layertype='wmtslayer';