-- This is a dummy migration that is needed because pti database has a reference to this migration that has been since removed from oskari-server
-- When migrating to Flyway 4.2.0 the migration fails if we have a reference to migration that is no longer present
select now();