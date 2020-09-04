package flyway.paikkis;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Removes config for keys "logInUrl", "loginUrl" and "registerUrl" from bundles "analyse", "personaldata" and "publisher2".
 * Since we don't need to have per appsetup configs, but can use a common login/registration url config
 */
public class V2_28__remove_login_register_configs_from_appsetups extends BaseJavaMigration {

    private static final Logger LOG = LogFactory.getLogger(V2_28__remove_login_register_configs_from_appsetups.class);
    private static final Set<String> BUNDLES = ConversionHelper.asSet("analyse", "personaldata", "publisher2");
    private static final Set<String> CONFIG_KEYS = ConversionHelper.asSet("logInUrl", "loginUrl", "registerUrl");

    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        for(String bundleName : BUNDLES) {
            Integer id = getBundleId(conn, bundleName);
            if (id == null) {
                LOG.info(bundleName, "bundle not found");
                continue;
            }
            int bundleId = id;
            LOG.debug(bundleName, "bundle id:", bundleId);
            List<BundleConfig> bundleConfigs = getBundleConfigs(conn, bundleId);

            List<BundleConfig> toUpdate = getBundleConfigsToUpdate(bundleConfigs, CONFIG_KEYS);
            update(conn, toUpdate, bundleId);
        }
    }


    private Integer getBundleId(Connection conn, String bundleId) throws SQLException {
        String sql = "SELECT id FROM portti_bundle WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bundleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
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

    protected static List<BundleConfig> getBundleConfigsToUpdate(List<BundleConfig> bundleConfigs, Set<String> keysToRemove) {
        List<BundleConfig> toUpdate = new ArrayList<>();

        for (BundleConfig bundleConfig : bundleConfigs) {
            if(bundleConfig.config == null) {
                continue;
            }
            boolean updated = false;
            for(String key : keysToRemove) {
                // remove keys from config
                if(bundleConfig.config.remove(key) != null) {
                    updated = true;
                }
            }
            if (updated) {
                toUpdate.add(bundleConfig);
            }
        }

        return toUpdate;
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
