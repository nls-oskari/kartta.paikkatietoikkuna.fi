package flyway.paikkis;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Removes all references of layers with IDs starting with "base_" as they have been replaced with actual ids in selected layers.
 */
public class V2_29__external_id_removal_from_configs extends BaseJavaMigration {

    private static final Logger LOG = LogFactory.getLogger(V2_29__external_id_removal_from_configs.class);

    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        Integer mapfullBundleId = getMapfullBundleId(conn);
        if (mapfullBundleId == null) {
            LOG.info("Mapfull bundle not found");
            return;
        }
        int bundleId = mapfullBundleId;
        LOG.debug("Mapfull bundle id:", bundleId);
        List<BundleConfig> bundleConfigs = getBundleConfigs(conn, bundleId);
        List<BundleConfig> toUpdate = getBundleConfigsToUpdate(bundleConfigs);
        update(conn, toUpdate, bundleId);
    }

    private Integer getMapfullBundleId(Connection conn) throws SQLException {
        String sql = "SELECT id FROM portti_bundle WHERE name = 'mapfull'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return null;
    }

    private List<BundleConfig> getBundleConfigs(Connection conn, int mapfullBundleId) throws SQLException {
        List<BundleConfig> configs = new ArrayList<>();

        String sql = "SELECT view_id, seqno, config FROM portti_view_bundle_seq WHERE bundle_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, mapfullBundleId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    BundleConfig config = new BundleConfig();
                    config.viewId = rs.getInt("view_id");
                    config.seqNo = rs.getInt("seqno");
                    config.config = JSONHelper.createJSONObject(rs.getString("config"));
                    configs.add(config);
                }
            }
        }

        return configs;
    }

    protected static List<BundleConfig> getBundleConfigsToUpdate(List<BundleConfig> bundleConfigs) {
        List<BundleConfig> toUpdate = new ArrayList<>();

        for (BundleConfig bundleConfig : bundleConfigs) {
            boolean updatedConfig = updateConfig(bundleConfig.config);
            if (updatedConfig) {
                toUpdate.add(bundleConfig);
            }
        }

        return toUpdate;
    }

    protected static boolean updateConfig(JSONObject config) {
        if (config == null) {
            return false;
        }
        try {
            JSONArray layers = config.optJSONArray("layers");
            if (layers == null) {
                return false;
            }
            JSONArray fixedLayers = new JSONArray();
            for (int i = 0; i < layers.length(); i++) {
                JSONObject layer = layers.optJSONObject(i);
                if (layer != null && !layer.optString("id").startsWith("base_")) {
                    fixedLayers.put(layer);
                }
            }
            config.put("layers", fixedLayers);
            return layers.length() != fixedLayers.length();
        } catch (JSONException e) {
            LOG.warn(e);
            return false;
        }
    }

    private void update(Connection conn, List<BundleConfig> bundleConfigs,
                        int mapfullBundleId) throws SQLException {
        final boolean oldAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            String sql = "UPDATE portti_view_bundle_seq SET config=? WHERE bundle_id=? AND view_id=? AND seqno=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(2, mapfullBundleId);
                for (BundleConfig bundleConfig : bundleConfigs) {
                    ps.setString(1, bundleConfig.config.toString());
                    ps.setInt(3, bundleConfig.viewId);
                    ps.setInt(4, bundleConfig.seqNo);
                    ps.addBatch();
                    LOG.debug(ps.toString());
                }
                ps.executeBatch();
                conn.commit();
            }
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }

    protected static class BundleConfig {
        int viewId;
        int seqNo;
        JSONObject config;
    }

}
