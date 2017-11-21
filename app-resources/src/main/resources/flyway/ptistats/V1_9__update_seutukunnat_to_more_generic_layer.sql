-- update layer name
UPDATE oskari_maplayer SET name='tilastointialueet:seutukunta4500k' where type = 'statslayer' and name = 'tilastointialueet:seutukunta1000k';
-- update resourcemapping to match the new name
UPDATE oskari_resource SET resource_mapping='statslayer+http://geo.stat.fi/geoserver/wms+tilastointialueet:seutukunta4500k' where resource_mapping = 'statslayer+http://geo.stat.fi/geoserver/wms+tilastointialueet:seutukunta1000k';
