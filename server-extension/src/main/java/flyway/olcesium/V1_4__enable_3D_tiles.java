package flyway.olcesium;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class V1_4__enable_3D_tiles implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_4__enable_3D_tiles.class);
    private static final String CESIUM_VIEW_NAME = "Geoportal Ol Cesium";
    private static final String MAP_BUNDLE_NAME = "mapfull";
    private static final String TILES_3D_LAYER_PLUGIN_ID = "Oskari.map3dtiles.bundle.tiles3d.plugin.Tiles3DLayerPlugin";
    private static final String TILES_3D_BUNDLE_NAME = "map3dtiles";
    private static final String TILES_3D_BUNDLE_PATH = "/Oskari/packages/paikkatietoikkuna/bundle/";

    private ViewService viewService = null;

    public void migrate(Connection connection) throws SQLException {
        viewService =  new ViewServiceIbatisImpl();
        updateCesiumViews(connection);
    }

    private void updateCesiumViews(Connection connection) {
        try {
            List<Long> viewIds = getCesiumViewIds(connection);
            LOG.info("Updating Cesium views - count:", viewIds.size());

            for(Long viewId : viewIds) {
                View modifyView = viewService.getViewWithConf(viewId);
                Bundle mapBundle = modifyView.getBundleByName(MAP_BUNDLE_NAME);

                if (mapBundle != null) {
                    JSONObject config = JSONHelper.createJSONObject(mapBundle.getConfig());
                    if (config != null) {
                        JSONArray plugins = config.optJSONArray("plugins");
                        if (plugins != null) {
                            // Add 3D tiles layer plugin
                            JSONObject tiles3dLayerPlugin = new JSONObject();
                            tiles3dLayerPlugin.put("id", TILES_3D_LAYER_PLUGIN_ID);
                            plugins.put(tiles3dLayerPlugin);

                            mapBundle.setConfig(config.toString());
                            viewService.updateBundleSettingsForView(viewId, mapBundle);
                        }
                    } else {
                        LOG.warn("Updating Cesium views / mapfull condig is null - view: ", viewId);
                    }

                    JSONObject startup = JSONHelper.createJSONObject(mapBundle.getStartup());
                    if (startup != null) {
                        JSONObject metadata = startup.optJSONObject("metadata");

                        if (metadata != null) {
                            JSONObject bundles = metadata.optJSONObject("Import-Bundle");
                            if (bundles != null) {
                                JSONObject bundle = JSONHelper.createJSONObject("bundlePath", TILES_3D_BUNDLE_PATH);
                                bundles.put(TILES_3D_BUNDLE_NAME, bundle);
                            }
                        }

                        mapBundle.setStartup(startup.toString());
                        viewService.updateBundleSettingsForView(viewId, mapBundle);

                    } else {
                        LOG.warn("Updating Cesium views / mapfull startup is null - view: ", viewId);
                    }
                } else {
                    LOG.warn("Updating Cesium views / mapfull bundle not found - view: ", viewId);
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Error setting selected layer for cesium views");
        }
    }

    private List<Long> getCesiumViewIds(Connection conn) throws SQLException {
        List<Long> list = new ArrayList<>();
        final String sql = "SELECT id FROM portti_view WHERE name=?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, CESIUM_VIEW_NAME);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getLong("id"));
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Error getting Cesium portti views");
        }
        return list;
    }
}
