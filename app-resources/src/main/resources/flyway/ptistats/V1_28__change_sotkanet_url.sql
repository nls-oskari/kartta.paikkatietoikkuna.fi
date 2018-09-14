UPDATE oskari_statistical_datasource SET config = replace(config, '"https://www.sotkanet.fi', '"https://sotkanet.fi') WHERE (config::json)->>'url' = 'https://www.sotkanet.fi/rest';
