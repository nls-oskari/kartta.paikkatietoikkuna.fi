package flyway.pti3d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import org.oskari.helpers.AppSetupHelper;

public class V1_6__add_camera_controls_3d_bundle_to_3D_views extends BaseJavaMigration {

	private static final String BUNDLE_ID = "camera-controls-3d";

	public void migrate(Context context) throws SQLException {
		Connection connection = context.getConnection();
		final List<Long> views = get3DApplicationViewIds(connection);
		for (Long viewId : views) {
			if (AppSetupHelper.appContainsBundle(connection, viewId, BUNDLE_ID)) {
				continue;
			}
			AppSetupHelper.addBundleToApp(connection, viewId, BUNDLE_ID);
		}
	}
	
	private static List<Long> get3DApplicationViewIds(Connection connection) throws SQLException {
        ArrayList<Long> ids = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id FROM portti_view WHERE application IN ('geoportal-3D','embedded-3D') AND type IN ('DEFAULT','PUBLISH')");

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql.toString())) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong("id"));
                }
            }
        }
        return ids;
    }
}
