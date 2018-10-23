package flyway.olcesium;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewException;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.FlywayHelper;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class V1_11__add_modules implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_11__add_modules.class);
    private static final String CESIUM_VIEW_NAME = "Geoportal Ol Cesium";
    private static final String MAP_BUNDLE_NAME = "mapfull";
    private static final int START_SEQNO = 10;
    private static final String[] MAP_PLUGIN_IDS = {
            "Oskari.mapframework.mapmodule.MarkersPlugin",
            "Oskari.mapframework.mapmodule.GetInfoPlugin",
            "Oskari.mapframework.bundle.mapmodule.plugin.RealtimePlugin",
            "Oskari.mapframework.bundle.myplacesimport.plugin.UserLayersLayerPlugin",
            "Oskari.mapframework.bundle.mapmodule.plugin.LogoPlugin",
            "Oskari.arcgis.bundle.maparcgis.plugin.ArcGisLayerPlugin",
            "Oskari.mapframework.bundle.mapmyplaces.plugin.MyPlacesLayerPlugin",
            "Oskari.mapframework.bundle.mapmodule.plugin.MyLocationPlugin"
    };
    private static final String[] BUNDLES_TO_ADD = {
            "infobox",
            "maplegend",
            "statsgrid",
            "metadataflyout",
            "routesearch",
            "userguide",
            "personaldata",
            "myplaces3",
            "guidedtour",
            "backendstatus",
            "printout",
            "metadatacatalogue",
            "myplacesimport",
            "findbycoordinates",
            "heatmap",
            "coordinatetool",
            "timeseries",
            "feedbackService",
            "maprotator",
            "register",
            "telemetry"
    };
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
                updateMapFullBundle(modifyView);
                addBundlesToView(connection, modifyView.getId());
            }
        } catch (Exception e) {
            LOG.error(e, "Could not update 3D view");
            throw new RuntimeException(e);
        }
    }

    private void updateMapFullBundle(View view) throws JSONException, ViewException {
        Bundle mapfull = view.getBundleByName(MAP_BUNDLE_NAME);
        JSONObject config = mapfull.getConfigJSON();
        JSONArray plugins = config.getJSONArray("plugins");
        Arrays.stream(MAP_PLUGIN_IDS).forEach(pluginId -> {
            try {
                JSONObject plugin = JSONHelper.createJSONObject("id", pluginId);
                if (pluginId.equals("Oskari.mapframework.mapmodule.GetInfoPlugin")) {
                    JSONArray ignored = new JSONArray();
                    ignored.put("WFS");
                    ignored.put("MYPLACES");
                    ignored.put("USERLAYER");
                    JSONObject pluginConf = new JSONObject();
                    pluginConf.put("ignoredLayerTypes", ignored);
                    pluginConf.put("infoBox", false);
                    plugin.put("config", pluginConf);
                }
                plugins.put(plugin);
            } catch (JSONException ex) {
                LOG.warn(ex, "Could not add map plugin " + pluginId + " to 3D view");
            }
        });
        mapfull.setConfig(config.toString());
        viewService.updateBundleSettingsForView(view.getId(), mapfull);
    }

    private void addBundlesToView(Connection conn, Long viewId) {
        Long defaultViewId = getDefaultViewId(conn);
        int bundleCounter = START_SEQNO;
        for (String bundleName : BUNDLES_TO_ADD) {
            try {
                if (!FlywayHelper.viewContainsBundle(conn, bundleName, viewId)) {
                    String conf = null;
                    Bundle bundle = FlywayHelper.getBundleFromView(conn, bundleName, defaultViewId);
                    if (bundle != null) {
                        conf = bundle.getConfig();
                    }
                    addBundle(conn, viewId, bundleName, bundleCounter, conf);
                    bundleCounter++;
                }
            }
            catch (SQLException ex) {
                LOG.warn(ex, "Could not add bundle " + bundleName + " to 3D view");
            }
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

    private Long getDefaultViewId(Connection conn) {
        final String sql = "select id from portti_view where type = 'DEFAULT' " +
                "and is_default = true order by usagecount desc limit 1";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Error getting default view id");
        }
        return null;
    }

    private void addBundle(Connection connection, Long viewId, String bundleName, int seqno, String config) throws SQLException {
        final String sql ="INSERT INTO portti_view_bundle_seq" +
                "(view_id, bundle_id, seqno, config, state, startup, bundleinstance) " +
                "VALUES (" +
                "?, " +
                "(SELECT id FROM portti_bundle WHERE name=?), " +
                "?, " +
                (config != null ? "?, " : "(SELECT config FROM portti_bundle WHERE name=?), ") +
                "(SELECT state FROM portti_bundle WHERE name=?),  " +
                "(SELECT startup FROM portti_bundle WHERE name=?), " +
                "?)";
        try(final PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = 0;
            statement.setLong(++i, viewId);
            statement.setString(++i, bundleName);
            statement.setInt(++i, seqno);
            statement.setString(++i, config != null ? config : bundleName);
            statement.setString(++i, bundleName);
            statement.setString(++i, bundleName);
            statement.setString(++i, bundleName);
            statement.execute();
        }
    }
}
