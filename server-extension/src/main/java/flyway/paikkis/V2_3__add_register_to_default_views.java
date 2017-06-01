package flyway.paikkis;

import fi.nls.oskari.util.FlywayHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.util.List;

/**
 * Adds register bundle to default and user views.
 */
public class V2_3__add_register_to_default_views implements JdbcMigration {
    private static final String BUNDLE_ID = "register";

    public void migrate(Connection connection) throws Exception {
        final List<Long> views = FlywayHelper.getUserAndDefaultViewIds(connection);
        for(Long viewId : views){
            if (FlywayHelper.viewContainsBundle(connection, BUNDLE_ID, viewId)) {
                continue;
            }
            FlywayHelper.addBundleWithDefaults(connection, viewId, BUNDLE_ID);
        }
    }
}
