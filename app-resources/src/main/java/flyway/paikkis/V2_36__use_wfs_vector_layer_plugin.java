package flyway.paikkis;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.helpers.AppSetupHelper;

import java.sql.Connection;
import java.util.List;

/**
 * Replaces transport based wfs plugin with vector implementation
 */
public class V2_36__use_wfs_vector_layer_plugin extends BaseJavaMigration {

    private static final Logger LOG = LogFactory.getLogger(V2_36__use_wfs_vector_layer_plugin.class);
    private static final String BUNDLE_NAME = "mapfull";
    private static final String WFS_TRANSPORT_PLUGIN_ID = "Oskari.mapframework.bundle.mapwfs2.plugin.WfsLayerPlugin";
    private static final String WFS_VECTOR_PLUGIN_ID = "Oskari.wfsvector.WfsVectorLayerPlugin";
    private static final String PLUGIN_CONFIG = "config";
    private static final String MIGRATION_PROP_NAME = "flyway.paikkis.useWfsVector";

    private ViewService viewService;

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        final boolean proceed = PropertyUtil.getOptional(MIGRATION_PROP_NAME, false);
        if (!proceed) {
            LOG.info("Skipping migration to wfs vector plugin");
            return;
        }
        try {
            viewService = new AppSetupServiceMybatisImpl();
            List<Long> viewIds = AppSetupHelper.getSetupsForType(connection);
            for (long id : viewIds) {
                updateView(connection, id);
            }
        }
        catch (Exception ex) {
            LOG.error("Migration failed", ex);
            throw ex;
        }
    }

    private void updateView (Connection connection, long viewId) throws Exception {
        Bundle mapfull = AppSetupHelper.getAppBundle(connection, viewId, BUNDLE_NAME);
        if (mapfull == null) {
            return;
        }
        JSONObject config = mapfull.getConfigJSON();
        if (config == null) {
            return;
        }
        JSONArray plugins = config.optJSONArray("plugins");
        if (plugins == null) {
            return;
        }
        for (int i = 0; i < plugins.length(); i++) {
            JSONObject plugin = plugins.getJSONObject(i);
            String id = plugin.optString("id");
            if (id == null || !id.equals(WFS_TRANSPORT_PLUGIN_ID)) {
                continue;
            }
            if (plugin.has(PLUGIN_CONFIG)) {
                plugin.remove(PLUGIN_CONFIG);
            }
            plugin.put("id", WFS_VECTOR_PLUGIN_ID);
        }
        mapfull.setConfig(config.toString());
        viewService.updateBundleSettingsForView(viewId, mapfull);
    }
}
