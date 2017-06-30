package flyway.ptimigration;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

/**
 * Removes mapUrlPrefix config from toolbar if available
 */
public class V1_11__Remove_mapurl_from_toolbar_config extends ConfigMigration {

    public String getBundle() {
        return "toolbar";
    }

    public String getModifiedConfig(final String param) throws Exception {
        final JSONObject config = JSONHelper.createJSONObject(param);
        config.remove("mapUrlPrefix");
        return config.toString(2);
    }

}
