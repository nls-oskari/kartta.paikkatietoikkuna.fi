package flyway.paikkis;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.FlywayHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.util.List;

/**
 * Registers and adds 'coordinatetransformation' bundle to default and user views.
 */
public class V2_35__add_coordinatetransformation_bundle implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V2_35__add_coordinatetransformation_bundle.class);
    private static final String BUNDLE_ID = "coordinatetransformation";

    public void migrate(Connection connection) throws Exception {
        // add bundle to default/user views
        final List<Long> views = FlywayHelper.getUserAndDefaultViewIds(connection);
        for(Long viewId : views){
            if (FlywayHelper.viewContainsBundle(connection, BUNDLE_ID, viewId)) {
                continue;
            }
            FlywayHelper.addBundleWithDefaults(connection, viewId, BUNDLE_ID);
        }
    }
}
