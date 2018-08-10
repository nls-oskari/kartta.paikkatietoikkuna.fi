package flyway.olcesium;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class V1_3__force_proxy implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(flyway.olcesium.V1_1__set_layers.class);
    private static final String CESIUM_VIEW_NAME = "Geoportal Ol Cesium";
    private static final String LAYERSELECTOR_BUNDLE_NAME = "layerselector2";
    private static final String MAPFULL_BUNDLE_NAME = "mapfull";

    private ViewService viewService = null;

    public void migrate(Connection connection) {
        viewService =  new ViewServiceIbatisImpl();
        updateCesiumViews(connection);
    }

    private void updateCesiumViews(Connection connection) {
        try {
            List<Long> viewIds = getCesiumViewIds(connection);
            LOG.info("Updating Cesium views - count:", viewIds.size());

            for(Long viewId : viewIds) {
                View modifyView = viewService.getViewWithConf(viewId);

                // update layer selector conf
                Bundle layerSelectorBundle = modifyView.getBundleByName(LAYERSELECTOR_BUNDLE_NAME);
                if (layerSelectorBundle != null) {
                    JSONObject config = JSONHelper.createJSONObject(layerSelectorBundle.getConfig());
                    if (config == null) {
                        config = new JSONObject();
                    }
                    JSONHelper.putValue(config, "forceProxy", true);
                    layerSelectorBundle.setConfig(config.toString());
                    viewService.updateBundleSettingsForView(viewId, layerSelectorBundle);
                } else {
                    LOG.warn("Updating Cesium views / layerselector2 bundle not found - view: ", viewId);
                }

                // update mapfull conf
                Bundle mapfullBundle = modifyView.getBundleByName(MAPFULL_BUNDLE_NAME);
                if (mapfullBundle != null) {
                    JSONObject config = JSONHelper.createJSONObject(mapfullBundle.getConfig());
                    if (config == null) {
                        config = new JSONObject();
                    }
                    JSONHelper.putValue(config, "forceProxy", true);
                    mapfullBundle.setConfig(config.toString());
                    viewService.updateBundleSettingsForView(viewId, mapfullBundle);
                } else {
                    LOG.warn("Updating Cesium views / mapfull bundle not found - view: ", viewId);
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Error setting proxy conf for cesium views");
        }
    }

    private List<Long> getCesiumViewIds(Connection conn) {
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
