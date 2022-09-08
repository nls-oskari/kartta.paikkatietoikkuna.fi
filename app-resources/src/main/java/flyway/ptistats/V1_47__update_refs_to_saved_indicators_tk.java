package flyway.ptistats;

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
 * Updates references to indicator ids after 1.44 migration since id also references px-file name that changes yearly
 */
public class V1_47__update_refs_to_saved_indicators_tk extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_47__update_refs_to_saved_indicators_tk.class);
    private static final String ID_SEPARATOR = "::";

    public void migrate(Context context) throws SQLException {
        Connection conn = context.getConnection();
        Integer statsgridBundleId = getStatsgridBundleId(conn);
        if (statsgridBundleId == null) {
            LOG.info("Statsgrid bundle not found");
            return;
        }
        int bundleId = statsgridBundleId;
        LOG.debug("Statsgrid bundle id:", bundleId);

        List<Bundle> bundleStates = getBundleStates(conn, bundleId);
        List<Bundle> toUpdate = getBundleStatesToUpdate(bundleStates);
        update(conn, toUpdate, bundleId);
    }

    // for overriding purposes next year...
    public String getNewFileName() {
        return "kuntien_avainluvut_2021_aikasarja.px";
    }

    protected String getNewId(String oldId) {
        String[] parts = oldId.split(ID_SEPARATOR);
        if (parts.length != 2) {
            // something weird here
            LOG.warn("Unidentified id:", oldId);
            return oldId;
        }
        return getNewFileName() + ID_SEPARATOR + parts[1];
    }
    private Integer getStatsgridBundleId(Connection conn) throws SQLException {
        String sql = "SELECT id FROM oskari_bundle WHERE name = 'statsgrid'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return null;
    }

    private List<Bundle> getBundleStates(Connection conn, int mapfullBundleId) throws SQLException {
        List<Bundle> configs = new ArrayList<>();

        String sql = "SELECT appsetup_id, seqno, state FROM oskari_appsetup_bundles WHERE bundle_id = ? " +
                "AND state LIKE '%kuntien_avainluvut%'";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, mapfullBundleId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Bundle config = new Bundle();
                    config.view = rs.getInt("appsetup_id");
                    config.seqno = rs.getInt("seqno");
                    config.state = JSONHelper.createJSONObject(rs.getString("state"));
                    configs.add(config);
                }
            }
        }

        return configs;
    }

    protected List<Bundle> getBundleStatesToUpdate(List<Bundle> bundleStates) {
        List<Bundle> toUpdate = new ArrayList<>();

        for (Bundle bundleConfig : bundleStates) {
            boolean updatedState = updateState(bundleConfig.state);
            if (updatedState) {
                toUpdate.add(bundleConfig);
            }
        }

        return toUpdate;
    }
/*
    {
        "regionset": 2027,
        "indicators": [{
            "id": "kuntien_avainluvut_2017_aikasarja.px::M508",
            ...
        }]
    }
*/

    protected boolean updateState(JSONObject state) {
        if (state == null) {
            return false;
        }
        JSONArray indicators = state.optJSONArray("indicators");
        if (indicators == null) {
            return false;
        }
        boolean replacedAtLeastOne = false;
        for (int i = 0; i < indicators.length(); i++) {
            JSONObject selectedIndicator = indicators.optJSONObject(i);
            String id = selectedIndicator.optString("id");
            try {
                String newId = getNewId(id);
                if (newId.equals(id)) {
                    // already valid
                    continue;
                }
                selectedIndicator.put("id", newId);
                replacedAtLeastOne = true;
            } catch (JSONException ex) {
                LOG.warn(ex);
            }
        }
        return replacedAtLeastOne;
    }

    private void update(Connection conn, List<Bundle> bundleConfigs,
                        int bundleId) throws SQLException {
        final boolean oldAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            String sql = "UPDATE oskari_appsetup_bundles SET state=? WHERE bundle_id=? AND appsetup_id=? AND seqno=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(2, bundleId);
                for (Bundle bundleConfig : bundleConfigs) {
                    ps.setString(1, bundleConfig.state.toString());
                    ps.setInt(3, bundleConfig.view);
                    ps.setInt(4, bundleConfig.seqno);
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

    class Bundle {
        int view;
        int seqno;
        JSONObject state;
    }
}
