package flyway.ptimigration;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by SMAKINEN on 26.5.2017.
 */
public class V1_0__drop_wfslayerplugin_config extends ConfigMigration {

    private static final String PLUGIN_NAME = "Oskari.mapframework.bundle.mapwfs2.plugin.WfsLayerPlugin";

    public String getModifiedConfig(final String param) throws Exception {
        final JSONObject config = JSONHelper.createJSONObject(param);
        final JSONArray plugins = config.optJSONArray("plugins");
        if(plugins == null) {
            throw new RuntimeException("No plugins" + config.toString(2));
        }
        for(int i = 0; i < plugins.length(); ++i) {
            JSONObject plugin = plugins.getJSONObject(i);
            if(PLUGIN_NAME.equals(plugin.optString("id"))) {
                plugin.remove("config");
                return config.toString(2);
            }
        }
        return null;
    }
}