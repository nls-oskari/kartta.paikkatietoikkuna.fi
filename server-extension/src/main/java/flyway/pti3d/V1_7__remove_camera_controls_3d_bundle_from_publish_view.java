package flyway.pti3d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import fi.nls.oskari.util.FlywayHelper;

public class V1_7__remove_camera_controls_3d_bundle_from_publish_view implements JdbcMigration {

	private static final String BUNDLE_ID = "camera-controls-3d";

	@Override
	public void migrate(Connection connection) throws Exception {
		final Long viewId = get3dPublishViewID(connection);
		if (FlywayHelper.viewContainsBundle(connection, BUNDLE_ID, viewId)) {
			FlywayHelper.removeBundleFromView(connection, BUNDLE_ID, viewId);
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
