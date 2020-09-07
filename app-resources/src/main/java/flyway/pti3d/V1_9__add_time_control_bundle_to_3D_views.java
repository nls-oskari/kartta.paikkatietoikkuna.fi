package flyway.pti3d;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppSetupHelper;

public class V1_9__add_time_control_bundle_to_3D_views extends BaseJavaMigration {
	private static final String BUNDLE_ID = "time-control-3d";
	private static final String APPLICATION_NAME = "geoportal-3D";

	public void migrate(Context context) throws SQLException {
		Connection conn = context.getConnection();
		final List<Long> views = AppSetupHelper.getSetupsForUserAndDefaultType(conn, APPLICATION_NAME);
		for (Long viewId : views) {
			if (AppSetupHelper.appContainsBundle(conn, viewId, BUNDLE_ID)) {
				continue;
			}
			AppSetupHelper.addBundleToApp(conn, viewId, BUNDLE_ID);
		}
	}
}
