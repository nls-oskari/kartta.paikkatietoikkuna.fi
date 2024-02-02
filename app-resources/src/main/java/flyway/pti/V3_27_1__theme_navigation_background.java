package flyway.pti;

import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;
import org.oskari.helpers.AppSetupHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Add new theme variable navigation.bg to portal.
 *
 * Version 3.27.1 where
 * 3 = oskari 2.x
 * 27 = paikkatietoikkuna minor version (1.27 atm)
 * 1 = running version number
 */

public class V3_27_1__theme_navigation_background extends BaseJavaMigration {
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        // update theme for geoportal views
        List<Long> ids = AppSetupHelper.getSetupsForType(connection, ViewTypes.DEFAULT, ViewTypes.USER);
        for (Long id: ids) {
            JSONObject metadata = getAppSetupMetadata(connection, id);
            JSONObject theme = metadata.getJSONObject("theme");

            if (theme == null) {
                continue;
            }

            JSONObject map = null;
            if (theme.has("map")) {
                map = theme.getJSONObject("map");
            } else {
                map = new JSONObject();
            }

            JSONObject navigation = null;
            if (map.has("navigation")) {
                navigation = map.getJSONObject("navigation");
            } else {
                navigation = new JSONObject();
                map.put("navigation", navigation);
            }

            JSONObject color = null;
            if (navigation.has("color")) {
                color = navigation.getJSONObject("color");
            } else {
                color = new JSONObject();
                navigation.put("color", color);
            }

            color.put("bg", "#3c3c3c");
            theme.put("map", map);

            JSONHelper.putValue(metadata, "theme", theme);
            updateAppMetadata(connection, id, metadata);
        }
    }

    private JSONObject getAppSetupMetadata(Connection conn, long id) throws SQLException {
        try (PreparedStatement statement = conn
                .prepareStatement("SELECT metadata FROM oskari_appsetup WHERE id=?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return JSONHelper.createJSONObject(rs.getString("metadata"));
            }
        }
    }

    private void updateAppMetadata(Connection connection, long viewId, JSONObject metadata)
            throws SQLException {
        final String sql = "UPDATE oskari_appsetup SET metadata=? WHERE id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, metadata.toString());
            statement.setLong(2, viewId);
            statement.execute();
        }
    }


}


