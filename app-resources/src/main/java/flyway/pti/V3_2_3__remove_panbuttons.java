package flyway.pti;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.helpers.AppSetupHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Remove panbuttons from all default/user appsetups (geoportal appsetups)
 */
public class V3_2_3__remove_panbuttons extends BaseJavaMigration {
    private static final String BUNDLE = "mapfull";

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        // embedded maps doesn't have transformations, migrate only default and user appsetups
        List<Long> viewIds =  AppSetupHelper.getSetupsForUserAndDefaultType(connection);
        for (Long id : viewIds) {
            updateAppsetup(connection, id);
        }
    }
    private void updateAppsetup(Connection connection, Long viewId) throws SQLException {
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
        for (int i = 0; i < plugins.length(); i++) {
            JSONObject plugin = plugins.optJSONObject(i);
            if (plugin == null) {
                continue;
            }
            if ("Oskari.mapframework.bundle.mapmodule.plugin.PanButtons".equals(plugin.optString("id"))) {
                index = i;
            }
        }
        if (index == -1) {
            return;
        }
        // plugin found -> remove and update
        plugins.remove(index);
        JSONHelper.put(conf, "plugins", plugins);
        bundle.setConfig(conf.toString());
        AppSetupHelper.updateAppBundle(connection, viewId, bundle);
    }
}
