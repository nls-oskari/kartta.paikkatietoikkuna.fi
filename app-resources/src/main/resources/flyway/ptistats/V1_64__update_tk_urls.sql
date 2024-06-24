-- Update URL for all layers from geo.stat.fi to us https and /geoserver/ows
update oskari_maplayer set url = 'https://geo.stat.fi/geoserver/ows' where type = 'statslayer' and url like '%geo.stat.fi%';

-- Remove featuresUrl from attributes so layer url is used as endpoint
update oskari_maplayer set attributes = '{"statistics":{"nameIdTag":"nimi","regionIdTag":"maakunta"}}' where type = 'statslayer' and name = 'tilastointialueet:maakunta4500k';
update oskari_maplayer set attributes = '{"statistics":{"nameIdTag":"nimi","regionIdTag":"ely"}}' where type = 'statslayer' and name = 'tilastointialueet:ely4500k';
update oskari_maplayer set attributes = '{"statistics":{"nameIdTag":"nimi","regionIdTag":"avi"}}' where type = 'statslayer' and name = 'tilastointialueet:avi4500k';
update oskari_maplayer set attributes = '{"statistics":{"nameIdTag":"nimi","regionIdTag":"kunta"}}' where type = 'statslayer' and name = 'tilastointialueet:kunta4500k';
update oskari_maplayer set attributes = '{"statistics":{"nameIdTag":"nimi","regionIdTag":"seutukunta"}}' where type = 'statslayer' and name = 'tilastointialueet:seutukunta4500k';
update oskari_maplayer set attributes = '{"statistics":{"regionIdTag":"hyvinvointialue","nameIdTag":"nimi"}}' where type = 'statslayer' and name = 'tilastointialueet:hyvinvointialue4500k';
update oskari_maplayer set attributes = '{"statistics":{"regionIdTag":"posti_alue","nameIdTag":"nimi"}}' where type = 'statslayer' and name = 'postialue:pno';
