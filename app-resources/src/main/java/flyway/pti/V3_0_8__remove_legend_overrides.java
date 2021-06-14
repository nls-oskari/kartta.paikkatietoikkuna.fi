package flyway.pti;

import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Removed layer.options.legends object (removing any override admin has given) for layers
 * returned by getLayersToModify().
 * This one removes overrides from layers with dataprovider 57 (Tilastokeskus)-
 */
public class V3_0_8__remove_legend_overrides extends BaseJavaMigration {

    class Result {
        int id;
        String options;
    }

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        List<Result> layers = getLayersToModify(connection);
        layers.forEach(r -> removeLegendOverrides(r));
        saveChanges(connection, layers);
    }

    private void removeLegendOverrides(Result result) {
        if (result.options == null) {
            return;
        }
        JSONObject options = JSONHelper.createJSONObject(result.options);
        if (options == null) {
            return;
        }
        options.remove("legends");
        result.options = options.toString();
    }

    public int getDataproviderToModify() {
        return 57;
    }

    public List<Result> getLayersToModify(Connection conn) throws Exception {
        List<Result> layers = new ArrayList<>();
        // remove legends override from layers by "Tilastokeskus"
        final String sql = "SELECT id, options FROM oskari_maplayer where dataprovider_id = " + getDataproviderToModify();
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    Result result = new Result();
                    result.id = rs.getInt("id");
                    result.options = rs.getString("options");
                    layers.add(result);
                }
            }
        }
        return layers;
    }

    private void saveChanges(Connection conn, List<Result> layers) throws SQLException {
        String sql = "UPDATE oskari_maplayer SET options = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Result layer : layers) {
                ps.setString(1, layer.options);
                ps.setInt(2, layer.id);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
