package flyway.olcesium;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMTS;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import fi.nls.oskari.wmts.domain.WMTSCapabilitiesLayer;
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
import java.util.Set;

public class V1_1__set_layers implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(flyway.olcesium.V1_1__set_layers.class);
    private static final String PROJECTION_PSEUDO_MERCATOR = "EPSG:3857";
    private static final String CESIUM_VIEW_NAME = "Geoportal Ol Cesium";
    private static final String MAP_BUNDLE_NAME = "mapfull";
    private static final String BASELAYER_PLUGIN_NAME = "BackgroundLayerSelectionPlugin";

    private ViewService viewService = null;
    private CapabilitiesCacheService capabilitiesService = null;

    public void migrate(Connection connection) throws SQLException {
        viewService =  new ViewServiceIbatisImpl();
        capabilitiesService = OskariComponentManager.getComponentOfType(CapabilitiesCacheService.class);
        List<OskariLayer> layers = getNlsBaseLayers(connection);
        LOG.info("Start populating matrixies for Oskari WMTS layers - count:", layers.size());
        for (OskariLayer layer : layers) {
            updateNlsBaseLayer(connection, layer);
        }
        if (!layers.isEmpty()) {
            updateCesiumViews(connection, layers);
        }
    }

    private void updateNlsBaseLayer(Connection connection, OskariLayer layer) {
        try {
            // update
            OskariLayerCapabilities caps = capabilitiesService.getCapabilities(layer);
            if (caps != null) {
                WMTSCapabilities parsed = WMTSCapabilitiesParser.parseCapabilities(caps.getData());
                if (parsed != null) {
                    WMTSCapabilitiesLayer capsLayer = parsed.getLayer(layer.getName());
                    if (capsLayer != null) {
                        JSONObject jscaps = LayerJSONFormatterWMTS.createCapabilitiesJSON(capsLayer, (Set)null);
                        if (jscaps != null) {
                            updateCapabilities(layer.getId(), jscaps, connection);
                        }else {
                            LOG.info("WMTSCapabilities / json create failed - layer: ", layer.getName());
                        }
                    } else {
                        LOG.info("WMTSCapabilities / layer parse failed - layer: ", layer.getName());
                    }
                } else {
                    LOG.info("WMTSCapabilities / Capabilities parse failed - layer: ", layer.getName());
                }
            } else {
                LOG.info("WMTSCapabilities / getCapabilities failed - layer: ", layer.getName());
            }
        } catch (Exception e) {
            LOG.error(e, "Error getting capabilities for layer", layer);
        }
    }

    private void updateCesiumViews(Connection connection, List<OskariLayer> layers) {
        try {
            List<View> views = getCesiumViews(connection);
            LOG.info("Updating Cesium views - count:", views.size());

            JSONArray selectedLayers = new JSONArray();
            JSONObject jsonLyr = new JSONObject();
            jsonLyr.put("id", layers.get(0).getId());
            selectedLayers.put(jsonLyr);

            JSONArray layerSelectionPluginLayers = new JSONArray();
            layers.stream().forEach((layer) -> {
                try {
                    layerSelectionPluginLayers.put(layer.getId());
                } catch (Exception ex) {
                    LOG.warn("Updating Cesium views / could not list layers to background selection plugin");
                }
            });

            for(View view : views) {
                View modifyView = viewService.getViewWithConf(view.getId());
                Bundle mapBundle = modifyView.getBundleByName(MAP_BUNDLE_NAME);
                if (mapBundle != null) {

                    JSONObject state = JSONHelper.createJSONObject(mapBundle.getState());
                    if (state != null) {
                        state.put("selectedLayers", selectedLayers);
                        mapBundle.setState(state.toString());
                        viewService.updateBundleSettingsForView(view.getId(), mapBundle);
                    } else {
                        LOG.warn("Updating Cesium views / mapfull state is null - view: ", view.getId());
                    }

                    JSONObject config = JSONHelper.createJSONObject(mapBundle.getConfig());
                    if (config != null) {
                        JSONArray plugins = config.optJSONArray("plugins");
                        if (plugins != null) {
                            for (int i = 0; i < plugins.length(); i++) {
                                JSONObject plugin = plugins.getJSONObject(i);
                                if (plugin.getString("id").contains(BASELAYER_PLUGIN_NAME)) {
                                    JSONObject pluginConf = plugin.optJSONObject("config");
                                    if (pluginConf == null) {
                                        pluginConf = new JSONObject();
                                        plugin.put("config", pluginConf);
                                    }
                                    pluginConf.put("baseLayers", layerSelectionPluginLayers);
                                }
                            }
                        }
                        mapBundle.setConfig(config.toString());
                        viewService.updateBundleSettingsForView(view.getId(), mapBundle);
                    } else {
                        LOG.warn("Updating Cesium views / mapfull condig is null - view: ", view.getId());
                    }

                } else {
                    LOG.warn("Updating Cesium views / mapfull bundle not found - view: ", view.getId());
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Error setting selected layer for cesium views");
        }
    }

    private List<View> getCesiumViews(Connection conn) throws SQLException {
        List<View> list = new ArrayList<>();
        final String sql = "SELECT id FROM portti_view WHERE name = '"+CESIUM_VIEW_NAME+"'";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    View view = new View();
                    view.setId(rs.getLong("id"));
                    list.add(view);
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Error getting Cesium portti views");
        }
        return list;
    }


    private void updateCapabilities(int layerId, JSONObject capabilities, Connection conn) throws SQLException {
        final String sql = "UPDATE oskari_maplayer SET capabilities=?, attributes=? where id=?";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, capabilities.toString(2));
            statement.setInt(2, layerId);
            statement.execute();
            statement.close();
        }
        catch (JSONException ignored) {
            LOG.error("Error updating oskari_maplayer.capabilities and attributes", layerId);
        }
    }

    private List<OskariLayer> getNlsBaseLayers(Connection conn) throws SQLException {
        List<OskariLayer> layers = new ArrayList<>();
        final String sql = String.join(" ",
                "SELECT lyr.id, lyr.name, lyr.url, lyr.type, lyr.username, lyr.password, lyr.srs_name, lyr.version",
                "FROM oskari_maplayer lyr, oskari_dataprovider dp",
                "where lyr.dataprovider_id = dp.id",
                "and dp.locale like '%Maanmittauslaitos%'",
                "and lyr.type = 'wmtslayer'",
                "and lyr.name in ('taustakartta', 'maastokartta')");
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
