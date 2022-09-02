-- remove backend status from apps
DELETE FROM oskari_appsetup_bundles where bundle_id=(select id from oskari_bundle where name ='backendstatus');
