package flyway.ptimigration;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Removed mapUrlPrefix and termsUrl config from LogoPlugin if available
 */
public class V1_1__Remove_LogoPlugin_config extends ConfigMigration {

    private static final String PLUGIN_NAME = "Oskari.mapframework.bundle.mapmodule.plugin.LogoPlugin";

    public String getModifiedConfig(final String param) throws Exception {
        final JSONObject config = JSONHelper.createJSONObject(param);
        final JSONArray plugins = config.optJSONArray("plugins");
        if (plugins == null) {
            throw new RuntimeException("No plugins" + config.toString(2));
        }
        for (int i = 0; i < plugins.length(); ++i) {
            JSONObject plugin = plugins.getJSONObject(i);
            if (PLUGIN_NAME.equals(plugin.optString("id"))) {
                JSONObject pluginConfig = plugin.optJSONObject("config");
                if (pluginConfig == null) {
                    break;
                }
                pluginConfig.remove("mapUrlPrefix");
                pluginConfig.remove("termsUrl");
                return config.toString(2);
            }
        }
        return null;
    }

}
