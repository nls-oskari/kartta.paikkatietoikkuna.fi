package flyway.pti3d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import org.oskari.helpers.AppSetupHelper;

public class V1_7__remove_camera_controls_3d_bundle_from_publish_view extends BaseJavaMigration {

	private static final String BUNDLE_ID = "camera-controls-3d";

	public void migrate(Context context) throws SQLException {
		Connection connection = context.getConnection();
		final Long viewId = get3dPublishViewID(connection);
		if (AppSetupHelper.appContainsBundle(connection, viewId, BUNDLE_ID)) {
			AppSetupHelper.removeBundleFromApp(connection, viewId, BUNDLE_ID);
		}
	}

	private static Long get3dPublishViewID(Connection connection) throws SQLException {
		StringBuilder sql = new StringBuilder(
				"SELECT id FROM portti_view where type='PUBLISH' AND application='embedded-3D'");

		try (final PreparedStatement statement = connection.prepareStatement(sql.toString())) {
			try (ResultSet rs = statement.executeQuery()) {
				if(rs.next()) {
					return rs.getLong("id");
				} else {
					/*
					 *  ID should be always found but just in case return this instead of null 
					 *  to prevent NullPointerExceptions in subsequent helper methods.
					 */
					return Long.MIN_VALUE;
				}
			}
		}
	}
}
