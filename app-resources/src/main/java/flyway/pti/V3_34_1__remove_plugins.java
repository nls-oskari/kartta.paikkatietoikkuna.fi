package flyway.pti;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.helpers.AppSetupHelper;
import fi.nls.oskari.domain.map.view.ViewTypes;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

/**
 * Remove unnecssary plugins from all appsetups
 */
public class V3_34_1__remove_plugins extends BaseJavaMigration {
    private static final String BUNDLE = "mapfull";

    public void migrate(Context context) throws Exception {

        Connection connection = context.getConnection();
        // migrate all appsetups
        List<Long> viewIds =  AppSetupHelper.getSetupsForType(connection,
            ViewTypes.DEFAULT, ViewTypes.USER, ViewTypes.PUBLISH_TEMPLATE, ViewTypes.PUBLISHED);
        List<String> pluginsToRemove = getPluginIdsToRemove();
        for (Long id : viewIds) {
            updateAppsetup(connection, id, pluginsToRemove);
        }
    }

    protected List<String> getPluginIdsToRemove () {
        List<String> pluginsToRemove = new ArrayList(7);
        // the ones related to this release
        pluginsToRemove.add("Oskari.mapframework.bundle.mapmyplaces.plugin.MyPlacesLayerPlugin");
        pluginsToRemove.add("Oskari.mapframework.bundle.myplacesimport.plugin.UserLayersLayerPlugin");
        // plugins that no longer need to be referenced manually after Oskari 3.2.0
        pluginsToRemove.add("Oskari.mapframework.wmts.mapmodule.plugin.WmtsLayerPlugin");
        pluginsToRemove.add("Oskari.mapframework.mapmodule.VectorLayerPlugin");
        pluginsToRemove.add("Oskari.mapframework.mapmodule.WmsLayerPlugin");
        pluginsToRemove.add("Oskari.mapframework.bundle.mapmodule.plugin.LayersPlugin");
        pluginsToRemove.add("Oskari.mapframework.mapmodule.BingMapsLayerPlugin");
        
        return pluginsToRemove;
    }

    private void updateAppsetup(Connection connection, Long viewId, List<String> pluginsToRemove) throws SQLException {
        Bundle bundle =  AppSetupHelper.getAppBundle(connection, viewId, BUNDLE);
        if (bundle == null) {
            return;
        }
        JSONObject conf = bundle.getConfigJSON();
        JSONArray plugins = conf.optJSONArray("plugins");
        if (plugins == null) {
            return;
        }
        int index = -1;
        JSONArray newPlugins = new JSONArray();
        for (int i = 0; i < plugins.length(); i++) {
            JSONObject plugin = plugins.optJSONObject(i);
            if (plugin == null) {
                continue;
            }
            
            if (!pluginsToRemove.contains(plugin.optString("id"))) {
                newPlugins.put(plugin);
            }

        }
        if (newPlugins.length() == plugins.length()) {
            return;
        }
        // plugins changed -> remove and update
        JSONHelper.put(conf, "plugins", newPlugins);
        bundle.setConfig(conf.toString());
        AppSetupHelper.updateAppBundle(connection, viewId, bundle);
    }
}
