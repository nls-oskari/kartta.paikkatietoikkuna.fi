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
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class V1_10__edit_default_layers_in_3D_view implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_10__edit_default_layers_in_3D_view.class);
    private static final String CESIUM_VIEW_NAME = "Geoportal Ol Cesium";
    private static final String MAP_BUNDLE_NAME = "mapfull";
    private static final String BASELAYER_PLUGIN_NAME = "BackgroundLayerSelectionPlugin";

    private ViewService viewService = null;

    public void migrate(Connection connection) throws SQLException {
        viewService =  new ViewServiceIbatisImpl();
        updateCesiumViews(connection);
    }

    private void updateCesiumViews(Connection connection) {
        try {
            List<Long> viewIds = getCesiumViewIds(connection);
            List<OskariLayer> layers = getNlsBaseLayers(connection);

            List layerIds = null;
            try {
                layerIds = layers.stream().map((layer) -> layer.getId()).collect(Collectors.toList());
            } catch (Exception e) {
                LOG.error(e, "Error adding layers to selection plugin for cesium views");
            }
            JSONArray layerSelectionPluginLayers = new JSONArray(layerIds);
            OskariLayer selectedLayer = layers.stream()
                    .filter((layer) -> layer.getName().equals("taustakartta"))
                    .findFirst()
                    .get();

            for(Long viewId : viewIds) {
                View modifyView = viewService.getViewWithConf(viewId);
                Bundle mapBundle = modifyView.getBundleByName(MAP_BUNDLE_NAME);
                if (mapBundle != null) {

                    JSONObject state = JSONHelper.createJSONObject(mapBundle.getState());
                    if (state != null && selectedLayer != null) {
                        JSONObject obj = JSONHelper.createJSONObject("id", selectedLayer.getId());
                        state.put("selectedLayers", JSONHelper.createJSONArray(obj));
                        mapBundle.setState(state.toString());
                        viewService.updateBundleSettingsForView(viewId, mapBundle);
                    }
                    if (layerSelectionPluginLayers != null) {
                        JSONObject mapFullConf = JSONHelper.createJSONObject(mapBundle.getConfig());
                        JSONObject plugin = this.getBackgroundLayerPlugin(mapFullConf);
                        if (plugin != null) {
                            JSONObject pluginConf = plugin.optJSONObject("config");
                            if (pluginConf == null) {
                                pluginConf = new JSONObject();
                                plugin.put("config", pluginConf);
                            }
                            pluginConf.put("baseLayers", layerSelectionPluginLayers);
                            pluginConf.put("showAsDropdown", false);
                            mapBundle.setConfig(mapFullConf.toString());
                            viewService.updateBundleSettingsForView(viewId, mapBundle);
                        }
                    }
                } else {
                    LOG.warn("Updating Cesium views / mapfull bundle not found - view: ", viewId);
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Error setting selected layer for cesium views");
        }
    }

    private JSONObject getBackgroundLayerPlugin(JSONObject mapFullConf) throws JSONException {
        if (mapFullConf != null) {
            JSONArray plugins = mapFullConf.optJSONArray("plugins");
            if (plugins != null) {
                for (int i = 0; i < plugins.length(); i++) {
                    JSONObject plugin = plugins.getJSONObject(i);
                    if (plugin.getString("id").contains(BASELAYER_PLUGIN_NAME)) {
                        return plugin;
                    }
                }
            }
        }
        return null;
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

    private List<OskariLayer> getNlsBaseLayers(Connection conn) throws SQLException {
        ArrayList<OskariLayer> layers = new ArrayList<>();
        final String sql = String.join(" ",
                "SELECT lyr.id, lyr.name, lyr.url, lyr.type, lyr.username, lyr.password, lyr.srs_name, lyr.version",
                "FROM oskari_maplayer lyr, oskari_dataprovider dp",
                "where lyr.dataprovider_id = dp.id",
                "and dp.locale like '%Maanmittauslaitos%'",
                "and lyr.type = 'wmtslayer'",
                "and (lyr.name = 'taustakartta' or lyr.name = 'maastokartta')",
                "and lyr.locale not like '%Taustakarttasarja%'");
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
                    layers.add(layer);
                }
            }
        }
        return layers;
    }

}
