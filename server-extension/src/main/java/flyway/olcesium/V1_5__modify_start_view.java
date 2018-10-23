package flyway.olcesium;

import fi.nls.oskari.domain.map.OskariLayer;
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

public class V1_5__modify_start_view implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_5__modify_start_view.class);
    private static final String CESIUM_VIEW_NAME = "Geoportal Ol Cesium";
    private static final String MAP_BUNDLE_NAME = "mapfull";
    private static final String GEOLOCATION_PLUGIN = "Oskari.mapframework.bundle.mapmodule.plugin.GeoLocationPlugin";

    private ViewService viewService = null;

    public void migrate(Connection connection) throws SQLException {
        viewService =  new ViewServiceIbatisImpl();
        updateCesiumViews(connection);
    }

    private void updateCesiumViews(Connection connection) throws SQLException {

        List<Long> viewIds = getCesiumViewIds(connection);
        OskariLayer baseLyr = getNlsBaseLayer(connection);

        try {
            for(Long viewId : viewIds) {
                View modifyView = viewService.getViewWithConf(viewId);
                Bundle mapBundle = modifyView.getBundleByName(MAP_BUNDLE_NAME);

                if (mapBundle != null) {
                    JSONObject bundleState = JSONHelper.createJSONObject(mapBundle.getState());
                    if (bundleState != null) {
                        // layers
                        JSONArray selectedLayers = new JSONArray();
                        if (baseLyr != null) {
                            selectedLayers.put(JSONHelper.createJSONObject("id", baseLyr.getId()));
                        }

                        bundleState.put("selectedLayers", selectedLayers);
                        // camera settings
                        JSONObject location = new JSONObject();
                        location.put("altitude", 1500000);
                        location.put("x", 2762500);
                        location.put("y", 7835000);
                        JSONObject orientation = new JSONObject();
                        orientation.put("heading", 0);
                        orientation.put("pitch", -65);
                        orientation.put("roll", 0);
                        JSONObject camera = new JSONObject();
                        camera.put("location", location);
                        camera.put("orientation", orientation);
                        bundleState.put("camera", camera);

                        mapBundle.setState(bundleState.toString());
                        viewService.updateBundleSettingsForView(viewId, mapBundle);
                    } else {
                        LOG.warn("Updating Cesium views / mapfull state is null - view: ", viewId);
                    }
                    JSONObject config = JSONHelper.createJSONObject(mapBundle.getConfig());
                    if (config != null) {
                        JSONArray plugins = config.optJSONArray("plugins");
                        if (plugins != null) {
                            // Add geolocation plugin
                            JSONObject geolocationPlugin = new JSONObject();
                            geolocationPlugin.put("id", GEOLOCATION_PLUGIN);
                            plugins.put(geolocationPlugin);

                        }

                        // Set min/max resolutions
                        JSONObject mapOptions = config.optJSONObject("mapOptions");
                        if (mapOptions != null) {
                            JSONArray resolutions = mapOptions.getJSONArray("resolutions");
                            if (resolutions != null) {
                                resolutions.remove(resolutions.length() - 1);
                                resolutions.put(0, "4096");
                            }
                        }

                        mapBundle.setConfig(config.toString());
                        viewService.updateBundleSettingsForView(viewId, mapBundle);

                    } else {
                        LOG.warn("Updating Cesium views / mapfull config is null - view: ", viewId);
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

    private OskariLayer getNlsBaseLayer(Connection conn) throws SQLException {
        final String sql = String.join(" ",
                "SELECT lyr.id, lyr.name, lyr.url, lyr.type, lyr.username, lyr.password, lyr.srs_name, lyr.version",
                "FROM oskari_maplayer lyr, oskari_dataprovider dp",
                "where lyr.dataprovider_id = dp.id",
                "and dp.locale like '%Maanmittauslaitos%'",
                "and lyr.type = 'wmtslayer'",
                "and lyr.name = 'taustakartta'",
                "limit 1");
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    OskariLayer layer = new OskariLayer();
                    layer.setId(rs.getInt("id"));
                    layer.setName(rs.getString("name"));
                    layer.setUrl(rs.getString("url"));
                    layer.setType(rs.getString("type"));
                    layer.setUsername(rs.getString("username"));
                    layer.setPassword(rs.getString("password"));
                    layer.setSrs_name(rs.getString("srs_name"));
                    layer.setVersion(rs.getString("version"));
                    return layer;
                }
            }
        }
        return null;
    }
}
