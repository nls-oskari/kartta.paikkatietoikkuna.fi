package flyway.ptistats;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
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
import java.util.Map;

/**
 * As Tilastokeskus PXWeb config is changed to parse a PX-table variables into indicators -> any saved indicator refs
 * need to be migrated to match the config.
 */
public class V1_23__update_tk_selected_indicators implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_23__update_tk_selected_indicators.class);

    public void migrate(Connection conn) throws SQLException {
        Integer statsgridBundleId = getStatsgridBundleId(conn);
        if (statsgridBundleId == null) {
            LOG.info("Statsgrid bundle not found");
            return;
        }
        int bundleId = statsgridBundleId;
        LOG.debug("Mapfull bundle id:", bundleId);

        List<Bundle> bundleStates = getBundleConfigs(conn, bundleId);
        List<Bundle> toUpdate = getBundleConfigsToUpdate(bundleStates);
        update(conn, toUpdate, bundleId);
    }

    private Integer getStatsgridBundleId(Connection conn) throws SQLException {
        String sql = "SELECT id FROM portti_bundle WHERE name = 'statsgrid'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return null;
    }

    private List<Bundle> getBundleConfigs(Connection conn, int mapfullBundleId) throws SQLException {
        List<Bundle> configs = new ArrayList<>();

        String sql = "SELECT view_id, seqno,  state FROM portti_view_bundle_seq WHERE bundle_id = ? " +
                "AND state LIKE '%kuntien_avainluvut_2017_viimeisin.px%' OR  state LIKE '%kuntien_avainluvut_2017_aikasarja.px%'";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, mapfullBundleId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Bundle config = new Bundle();
                    config.view = rs.getInt("view_id");
                    config.seqno = rs.getInt("seqno");
                    config.state = JSONHelper.createJSONObject(rs.getString("state"));
                    configs.add(config);
                }
            }
        }

        return configs;
    }

    protected static List<Bundle> getBundleConfigsToUpdate(List<Bundle> bundleConfigs) {
        List<Bundle> toUpdate = new ArrayList<>();

        for (Bundle bundleConfig : bundleConfigs) {
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
	"view": true,
	"active": "3_kuntien_avainluvut_2017_aikasarja.px_Tiedot=\"M408\":Vuosi=\"1987\"",
	"indicators": [{
		"selections": {
			"Vuosi": "1987",
			"Tiedot": "M408"
		},
		"id": "kuntien_avainluvut_2017_aikasarja.px",
		"classification": {
			"mode": "discontinuous",
			"method": "jenks",
			"count": 5,
			"name": "Blues",
			"type": "seq",
			"reverseColors": false
		},
		"ds": 3
	}]
}


{
	"regionset": 2027,
	"view": true,
	"active": "3_kuntien_avainluvut_2017_viimeisin.px_Tiedot=\"M391\"",
	"indicators": [{
		"selections": {
			"Tiedot": "M408"
		},
		"id": "kuntien_avainluvut_2017_viimeisin.px",
		"classification": {
			"mode": "distinct",
			"showValues": false,
			"method": "quantile",
			"mapStyle": "choropleth",
			"count": 5,
			"name": "Blues",
			"type": "seq",
			"reverseColors": false
		},
		"ds": 3
	}, {
		"selections": {
			"Tiedot": "M476"
		},
		"id": "kuntien_avainluvut_2017_viimeisin.px",
		"classification": {
			"mode": "discontinuous",
			"method": "jenks",
			"count": 5,
			"name": "Blues",
			"type": "seq",
			"reverseColors": false
		},
		"ds": 3
	}, {
		"selections": {
			"Tiedot": "M391"
		},
		"id": "kuntien_avainluvut_2017_viimeisin.px",
		"classification": {
			"mode": "discontinuous",
			"method": "jenks",
			"count": 5,
			"name": "Blues",
			"type": "seq",
			"reverseColors": false
		},
		"ds": 3
	}]
}
 */
    protected static boolean updateState(JSONObject state) {
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
                if("kuntien_avainluvut_2017_aikasarja.px".equals(id) && migrateTimeSeries(selectedIndicator)) {
                    // migrate selections.Tiedot -> id::value
                    replacedAtLeastOne = true;
                } else if ("kuntien_avainluvut_2017_viimeisin.px".equals(id) && migrateLatest(selectedIndicator)) {
                    // migrate id to aikasarja with selections.Vuosi -> 2017
                    replacedAtLeastOne = true;
                } else {
                    continue;
                }
                replacedAtLeastOne = true;
            } catch (JSONException ex) {
                LOG.warn(ex);
            }
        }
        return replacedAtLeastOne;
    }

    /*

FROM:
{
	"selections": {
		"Vuosi": "1987",
		"Tiedot": "M408"
	},
	"id": "kuntien_avainluvut_2017_aikasarja.px",
	...
}

TO:
{
	"selections": {
		"Vuosi": "1987"
	},
	"id": "kuntien_avainluvut_2017_aikasarja.px::M408",
	...
}
     */
    private static boolean migrateTimeSeries(JSONObject indicator) throws JSONException {
        JSONObject selections = indicator.optJSONObject("selections");
        if(selections == null) {
            return false;
        }
        // remove selection Tiedot as it's merged into id
        String idPostfix = (String) selections.remove("Tiedot");
        if(idPostfix == null) {
            return false;
        }
        String id = indicator.optString("id");
        indicator.put("id", id + "::" + idPostfix);
        return true;
    }
/*
FROM:
{
	"selections": {
		"Tiedot": "M408"
	},
	"id": "kuntien_avainluvut_2017_viimeisin.px",
	...
}

TO:
{
	"selections": {
		"Vuosi": "2017"
	},
	"id": "kuntien_avainluvut_2017_aikasarja.px::M408",
	...
}
 */
    private static boolean migrateLatest(JSONObject indicator) throws JSONException {
        JSONObject selections = indicator.optJSONObject("selections");
        if(selections == null) {
            return false;
        }
        String idPostfix = (String) selections.remove("Tiedot");
        if(idPostfix == null) {
            return false;
        }
        // inject year selection
        selections.put("Vuosi", "2017");
        indicator.put("id", "kuntien_avainluvut_2017_aikasarja.px::" + idPostfix);
        return true;
    }

    private void update(Connection conn, List<Bundle> bundleConfigs,
                        int bundleId) throws SQLException {
        final boolean oldAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            String sql = "UPDATE portti_view_bundle_seq SET state=? WHERE bundle_id=? AND view_id=? AND seqno=?";
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
