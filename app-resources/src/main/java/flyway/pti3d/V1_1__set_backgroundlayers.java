package flyway.pti3d;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWMTS;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
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
import java.util.stream.Collectors;

public class V1_1__set_backgroundlayers implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_1__set_backgroundlayers.class);
    private static final String APPLICATION_3D_NAME = "geoportal-3D";
    private static final String MAP_BUNDLE_NAME = "mapfull";
    private static final String BASELAYER_PLUGIN_NAME = "BackgroundLayerSelectionPlugin";
    private static final String BACKGROUND_LAYER_NAME = "taustakartta";
    private static final String TERRAIN_LAYER_NAME = "maastokartta";

    private ViewService viewService = null;
    private CapabilitiesCacheService capabilitiesService = null;

    public void migrate(Connection connection) throws SQLException {
        viewService =  new AppSetupServiceMybatisImpl();
        capabilitiesService = OskariComponentManager.getComponentOfType(CapabilitiesCacheService.class);
        List<OskariLayer> layers = getNlsBaseLayers(connection);
        LOG.info("Start populating matrixies for Oskari WMTS layers - count:", layers.size());
        for (OskariLayer layer : layers) {
            updateNlsBaseLayer(connection, layer);
        }
        if (!layers.isEmpty()) {
            updateLayerSelections(connection, layers);
        }
    }

    private void updateNlsBaseLayer(Connection connection, OskariLayer layer) {
        try {
            // update capabilities in cache to get epsg:3587 support
            String data = CapabilitiesCacheService.getFromService(layer);
            OskariLayerCapabilities caps = new OskariLayerCapabilities(
                    layer.getSimplifiedUrl(true),
                    layer.getType(),
                    layer.getVersion(),
                    data);

            WMTSCapabilities parsed = WMTSCapabilitiesParser.parseCapabilities(caps.getData());
            WMTSCapabilitiesLayer capsLayer = parsed.getLayer(layer.getName());

            JSONObject jscaps = null;
            if (capsLayer != null) {
                jscaps = LayerJSONFormatterWMTS.createCapabilitiesJSON(capsLayer, (Set)null);
            } else {
                LOG.info("WMTSCapabilities / layer parse failed - layer: ", layer.getName());
                return;
            }

            updateCapabilities(layer.getId(), jscaps, connection);
            capabilitiesService.save(caps);

        } catch (Exception e) {
            LOG.error(e, "Error getting capabilities for layer", layer);
        }
    }

    private void updateCapabilities(int layerId, JSONObject capabilities, Connection conn) throws SQLException {
        final String sql = "UPDATE oskari_maplayer SET capabilities=? where id=?";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, capabilities.toString(2));
            statement.setInt(2, layerId);
            statement.execute();
        }
        catch (JSONException ignored) {
            LOG.error("Error updating oskari_maplayer.capabilities", layerId);
        }
    }

    private void updateLayerSelections(Connection connection, List<OskariLayer> layers) {
        try {
            List<Long> viewIds = get3DApplicationViews(connection);
            List lyrIds = null;
            Integer selectedLayerId = null;
            try {
                lyrIds = layers.stream().map((layer) -> layer.getId()).collect(Collectors.toList());
                selectedLayerId = layers.stream()
                        .filter(layer -> layer.getName().equals(BACKGROUND_LAYER_NAME))
                        .map(layer -> layer.getId())
                        .findFirst()
                        .orElse(null);
            } catch (Exception e) {
                LOG.error(e, "Error adding layers to selection plugin for 3D views");
            }
            JSONArray baseLayers = new JSONArray(lyrIds);

            for(Long viewId : viewIds) {
                updateView(viewId, baseLayers, selectedLayerId);
            }
        } catch (Exception e) {
            LOG.error(e, "Error setting selected layer for 3D views");
        }
    }

    private void updateView(Long viewId, JSONArray baseLayers, int selected) throws Exception {
        View modifyView = viewService.getViewWithConf(viewId);

        Bundle mapBundle = modifyView.getBundleByName(MAP_BUNDLE_NAME);
        if (mapBundle == null) {
            LOG.warn("Updating 3D view. Mapfull bundle not found for view: ", viewId);
            return;
        }

        JSONObject state = JSONHelper.createJSONObject(mapBundle.getState());
        if (state != null) {
            JSONArray selectedLayers = new JSONArray();
            selectedLayers.put(JSONHelper.createJSONObject("id", selected));
            state.put("selectedLayers", selectedLayers);
            mapBundle.setState(state.toString());
            viewService.updateBundleSettingsForView(viewId, mapBundle);
        } else {
            LOG.warn("Updating 3D view. Mapfull state is null for view: ", viewId);
        }

        JSONObject config = JSONHelper.createJSONObject(mapBundle.getConfig());
        if (config == null) {
            LOG.warn("Updating 3D view failed. Mapfull config is null for view: ", viewId);
            return;
        }
        JSONArray plugins = config.optJSONArray("plugins");
        if (plugins == null) {
            return;
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
            return;
        }
        JSONObject pluginConf = layerSelectionPlugin.optJSONObject("config");
        if (pluginConf == null) {
            pluginConf = new JSONObject();
            layerSelectionPlugin.put("config", pluginConf);
        }
        pluginConf.put("baseLayers", baseLayers);

        mapBundle.setConfig(config.toString());
        viewService.updateBundleSettingsForView(viewId, mapBundle);
    }

    private List<Long> get3DApplicationViews(Connection conn) {
        List<Long> list = new ArrayList<>();
        final String sql = "SELECT id FROM portti_view WHERE application=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, APPLICATION_3D_NAME);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getLong("id"));
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Error getting 3D views");
        }
        return list;
    }

    private List<OskariLayer> getNlsBaseLayers(Connection conn) throws SQLException {
        List<OskariLayer> layers = new ArrayList<>();
        final String sql = String.join(" ",
                "SELECT lyr.id, lyr.name, lyr.url, lyr.type, lyr.username, lyr.password, lyr.srs_name, lyr.version",
                "FROM oskari_maplayer lyr, oskari_dataprovider dp",
                "where lyr.dataprovider_id = dp.id",
                "and dp.locale like '%Maanmittauslaitos%'",
                "and lyr.type = 'wmtslayer'",
                "and lyr.name in (?, ?)");
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, BACKGROUND_LAYER_NAME);
            statement.setString(2, TERRAIN_LAYER_NAME);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
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
