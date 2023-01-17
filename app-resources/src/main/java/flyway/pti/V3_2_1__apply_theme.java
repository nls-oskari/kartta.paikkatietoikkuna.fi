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
 * Change map popup colors for geoportal apps on PTI
 */
public class V3_2_1__apply_theme extends BaseJavaMigration {

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        // update theme for geoportal views
        List<Long> ids = AppSetupHelper.getSetupsForType(connection, ViewTypes.DEFAULT, ViewTypes.USER);
        for (Long id: ids) {
            JSONObject metadata = getAppSetupMetadata(connection, id);
            JSONObject theme = generateTheme();
            JSONHelper.putValue(metadata, "theme", theme);
            updateAppMetadata(connection, id, metadata);
        }
    }

    protected JSONObject generateTheme() {
        JSONObject theme = new JSONObject();

        JSONObject geoportalColor = new JSONObject();
        JSONHelper.putValue(theme, "color", geoportalColor);
        JSONHelper.putValue(geoportalColor, "primary", "#fdf8d9");
        JSONHelper.putValue(geoportalColor, "accent", "#ffd400");
        JSONHelper.putValue(geoportalColor, "text", "#3c3c3c");

        JSONObject mapTheme = new JSONObject();
        JSONHelper.putValue(theme, "map", mapTheme);
        JSONHelper.putValue(mapTheme, "font", "arial");

        JSONObject nav = new JSONObject();
        JSONHelper.putValue(mapTheme, "navigation", nav);
        JSONHelper.putValue(nav, "roundness", 100);
        JSONHelper.putValue(nav, "opacity", 0.8);

        JSONObject navColor = new JSONObject();
        JSONHelper.putValue(nav, "color", navColor);
        JSONHelper.putValue(navColor, "primary", "#141414");
        JSONHelper.putValue(navColor, "accent", "#ffd400");
        JSONHelper.putValue(navColor, "text", "#FFFFFF");

        JSONObject mainColor = new JSONObject();
        JSONHelper.putValue(mapTheme, "color", mainColor);
        JSONHelper.putValue(mainColor, "accent", "#ffd400");
        JSONObject headerColor = new JSONObject();
        JSONHelper.putValue(mainColor, "header", headerColor);
        JSONHelper.putValue(headerColor, "bg", "#fdf8d9");
        JSONHelper.putValue(headerColor, "text", "#3c3c3c");
        JSONHelper.putValue(headerColor, "icon", "#3c3c3c");

        return theme;
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
