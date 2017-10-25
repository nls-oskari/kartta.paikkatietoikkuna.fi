-- The only config for myplaces2 bundles in pti is measuretools:true (from ptimigration) - removing that leaves an empty config
-- This is because guest view was dropped -> myplaces should no longer offer measure tools for any user
-- Instead the common measuretools are shown to all users (not just guests like before)
UPDATE portti_view_bundle_seq SET config = '{}' where bundle_id = (select id from portti_bundle where name = 'myplaces2');