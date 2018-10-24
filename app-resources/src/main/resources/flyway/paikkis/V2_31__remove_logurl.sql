UPDATE portti_view_bundle_seq SET config = regexp_replace(config, '"logUrl"\s?:\s?"[^"]*"', '') WHERE bundleinstance = 'statehandler' AND (config::json->'logUrl')::text IS NOT NULL;
