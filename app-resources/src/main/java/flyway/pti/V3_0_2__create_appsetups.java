package flyway.pti;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.helpers.AppSetupHelper;
import org.oskari.helpers.LayerHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Add default layers and appsetups IF they are not in the database yet.
 */
public class V3_0_2__create_appsetups extends BaseJavaMigration {

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!AppSetupHelper.getSetupsForApplicationByType(connection, "geoportal", ViewTypes.DEFAULT).isEmpty()) {
            // database has the default appsetups no need to add them!
           return;
        }
        // Add initial content:
        String[] appSetupFiles = {"geoportal.json", "publish-template.json", "geoportal-3D.json"};
        String[] layerFiles = {"maastokartta.json", "ortokuva.json", "taustakartta.json"};

        // insert layers before appsetups to ensure supported projections is updated for 3D
        List<Integer> layerIds = new ArrayList<>();
        for(String file : layerFiles) {
            layerIds.add(LayerHelper.setupLayer("/json/layers/" + file));
        }

        for(String file : appSetupFiles) {
            AppSetupHelper.create(connection, "/json/views/" + file);
        }

        addDimensionChangeTo2D(connection);
        addDimensionChangeTo3D(connection);
        setup3Dpublishing(connection);
        setupBaselayersPlugin(connection, layerIds);
    }

    private void addDimensionChangeTo2D(Connection connection) throws SQLException {
        String BUNDLE_NAME = "dimension-change";
        String APPLICATION_3D_NAME = "geoportal-3D";
        String uuid = AppSetupHelper.getUuidForDefaultSetup(connection, APPLICATION_3D_NAME);
        List<Long> viewIds = AppSetupHelper.getSetupsForUserAndDefaultType(connection);

        for (Long id : viewIds) {
            if (!AppSetupHelper.appContainsBundle(connection, id, BUNDLE_NAME)) {
                AppSetupHelper.addBundleToApp(connection, id, BUNDLE_NAME);
                Bundle bundle = AppSetupHelper.getAppBundle(connection, id, BUNDLE_NAME);
                bundle.setConfig(JSONHelper.createJSONObject("uuid", uuid).toString());
                AppSetupHelper.updateAppBundle(connection, id, bundle);
            }
        }
    }

    private void addDimensionChangeTo3D(Connection connection) throws SQLException {
        String BUNDLE_NAME = "dimension-change";
        String APPLICATION_2D_NAME = "geoportal";
        String APPLICATION_3D_NAME = "geoportal-3D";
        String uuid = AppSetupHelper.getUuidForDefaultSetup(connection, APPLICATION_2D_NAME);
        final List<Long> viewIds = AppSetupHelper.getSetupsForUserAndDefaultType(connection, APPLICATION_3D_NAME);

        for (Long id : viewIds) {
            if (!AppSetupHelper.appContainsBundle(connection, id, BUNDLE_NAME)) {
                AppSetupHelper.addBundleToApp(connection, id, BUNDLE_NAME);
            }
            Bundle bundle = AppSetupHelper.getAppBundle(connection, id, BUNDLE_NAME);
            bundle.setConfig(JSONHelper.createJSONObject("uuid", uuid).toString());
            AppSetupHelper.updateAppBundle(connection, id, bundle);
        }
    }

    private void setup3Dpublishing(Connection connection) throws Exception {

        ViewService viewService = new AppSetupServiceMybatisImpl();
        String TEMPLATE_JSON = "publish-template-3D.json";
        String METADATA_TEMPLATE_KEY = "publishTemplateUuid";
        String APPLICATION_3D_NAME = "geoportal-3D";

        // Create 3D publish template view
        long templateViewId = AppSetupHelper.create(connection, TEMPLATE_JSON);
        View templateView = viewService.getViewWithConf(templateViewId);

        // Set it as the publication template for the default 3D view.
        View geoportalView = viewService.getViewWithConfByUuId(AppSetupHelper.getUuidForDefaultSetup(connection, APPLICATION_3D_NAME));
        geoportalView.getMetadata().put(METADATA_TEMPLATE_KEY, templateView.getUuid());
        viewService.updateView(geoportalView);
    }

    private void setupBaselayersPlugin(Connection conn, List<Integer> layers) throws Exception {
        String BASELAYER_PLUGIN_NAME = "BackgroundLayerSelectionPlugin";
        String MAP_BUNDLE_NAME = "mapfull";
        JSONArray baseLayers = new JSONArray(layers);
        List<Long> appSetups = AppSetupHelper.getSetupsForUserAndDefaultType(conn);
        for (long id: appSetups) {
            Bundle mapBundle = AppSetupHelper.getAppBundle(conn, id, MAP_BUNDLE_NAME);
            JSONObject config = mapBundle.getConfigJSON();
            if (config == null) {
                continue;
            }

            JSONArray plugins = config.optJSONArray("plugins");
            if (plugins == null) {
                continue;
            }
            JSONObject layerSelectionPlugin = null;
            for (int i = 0; i < plugins.length(); i++) {
                JSONObject plugin = plugins.getJSONObject(i);
                if (plugin.getString("id").contains(BASELAYER_PLUGIN_NAME)) {
                    layerSelectionPlugin = plugin;
                    break;
                }
            }
            if (layerSelectionPlugin == null) {
                continue;
            }
            JSONObject pluginConf = layerSelectionPlugin.optJSONObject("config");
            if (pluginConf == null) {
                pluginConf = new JSONObject();
                layerSelectionPlugin.put("config", pluginConf);
            }
            pluginConf.put("baseLayers", baseLayers);

            mapBundle.setConfig(config.toString());
            AppSetupHelper.updateAppBundle(conn, id, mapBundle);
        }
    }
}
