package flyway.pti3d;

import java.sql.Connection;
import java.util.List;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import fi.nls.oskari.util.FlywayHelper;

public class V1_9__add_shadow_bundle_to_3D_views implements JdbcMigration{
	private static final String BUNDLE_ID = "shadow-plugin-3d";
	private static final String APPLICATION_NAME = "geoportal-3D";
	
	@Override
	public void migrate(Connection conn) throws Exception {
		final List<Long> views = FlywayHelper.getUserAndDefaultViewIds(conn, APPLICATION_NAME);
		for (Long viewId : views) {
			if (FlywayHelper.viewContainsBundle(conn, BUNDLE_ID, viewId)) {
				continue;
			}
			FlywayHelper.addBundleWithDefaults(conn, viewId, BUNDLE_ID);
		}
	}
}
