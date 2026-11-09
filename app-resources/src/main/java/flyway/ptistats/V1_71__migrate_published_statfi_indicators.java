package flyway.ptistats;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Updates published statsgrid references from 12f7.px and 142h.px to their
 * 2026 tables. Datasource configurations must be updated separately; selections,
 * classifications and regionsets are preserved.
 */
public class V1_71__migrate_published_statfi_indicators extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_71__migrate_published_statfi_indicators.class);
    private static final int BATCH_SIZE = 100;
    private static final String OLD_POSTAL_TABLE = "12f7.px";
    private static final String NEW_POSTAL_TABLE = "12f7_ja2026.px";
    private static final String OLD_MUNICIPAL_TABLE = "142h.px";
    private static final String NEW_MUNICIPAL_TABLE = "142h_al2026.px";

    @Override
    public void migrate(Context context) throws SQLException {
        Connection connection = context.getConnection();
        String select = """
                SELECT bundles.appsetup_id, bundles.bundle_id, bundles.state
                FROM oskari_appsetup_bundles bundles
                JOIN oskari_appsetup setup ON setup.id = bundles.appsetup_id
                JOIN oskari_bundle bundle ON bundle.id = bundles.bundle_id
                WHERE setup.type = 'PUBLISHED' AND bundle.name = 'statsgrid'
                """;
        String update = """
                UPDATE oskari_appsetup_bundles SET state = ?
                WHERE appsetup_id = ? AND bundle_id = ?
                """;
        int changed = 0;
        try (PreparedStatement read = connection.prepareStatement(select);
             PreparedStatement write = connection.prepareStatement(update)) {
            read.setFetchSize(BATCH_SIZE);
            try (ResultSet rs = read.executeQuery()) {
                while (rs.next()) {
                    String json = rs.getString("state");
                    if (json == null || json.isBlank()) {
                        continue;
                    }
                    JSONObject state;
                    try {
                        state = new JSONObject(json);
                        if (!migrateState(state)) {
                            continue;
                        }
                    } catch (JSONException e) {
                        throw new SQLException("Invalid statsgrid state for appsetup "
                                + rs.getLong("appsetup_id"), e);
                    }
                    write.setString(1, state.toString());
                    write.setLong(2, rs.getLong("appsetup_id"));
                    write.setLong(3, rs.getLong("bundle_id"));
                    write.addBatch();
                    changed++;
                    if (changed % BATCH_SIZE == 0) {
                        write.executeBatch();
                        write.clearBatch();
                    }
                }
            }
            if (changed % BATCH_SIZE != 0) {
                write.executeBatch();
            }
        }
        LOG.info("Migrated StatFi indicator references in", changed, "published statsgrid states");
    }

    static boolean migrateState(JSONObject state) {
        boolean changed = false;
        if (!state.isNull("indicators")) {
            JSONArray indicators = state.getJSONArray("indicators");
            for (int i = 0; i < indicators.length(); i++) {
                JSONObject indicator = indicators.getJSONObject(i);
                String id = indicator.getString("id");
                String migratedId = migrateIndicatorId(id);
                if (!id.equals(migratedId)) {
                    indicator.put("id", migratedId);
                    changed = true;
                }
            }
        }
        if (!state.isNull("active")) {
            String active = state.getString("active");
            int separator = active.indexOf('_');
            if (separator > 0) {
                String id = active.substring(separator + 1);
                String migratedId = migrateIndicatorId(id);
                if (!id.equals(migratedId)) {
                    state.put("active", active.substring(0, separator + 1) + migratedId);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static String migrateIndicatorId(String id) {
        if (id.startsWith(OLD_POSTAL_TABLE + "::")) {
            return NEW_POSTAL_TABLE + id.substring(OLD_POSTAL_TABLE.length());
        }
        if (id.startsWith(OLD_MUNICIPAL_TABLE + "::")) {
            return NEW_MUNICIPAL_TABLE + id.substring(OLD_MUNICIPAL_TABLE.length());
        }
        return id;
    }
}
