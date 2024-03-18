-- This will remove mappings to region sets as well. We will add HKI again after we have proper mappings to region sets available
delete from oskari_statistical_datasource where locale like '%City of Helsinki%'
