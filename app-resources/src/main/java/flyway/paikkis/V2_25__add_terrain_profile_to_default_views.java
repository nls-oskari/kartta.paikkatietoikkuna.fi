package flyway.paikkis;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.util.FlywayHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.util.List;

/**
 * Registers and adds 'terrain-profile' bundle to default and user views.
 */
public class V2_25__add_terrain_profile_to_default_views implements JdbcMigration {
    private static final String BUNDLE_ID = "terrain-profile";

    public void migrate(Connection connection) throws Exception {

        // register bundle
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_ID);
        bundle.setStartup(BundleHelper.getBundleStartup("/Oskari/packages/paikkatietoikkuna/bundle/", BUNDLE_ID, BUNDLE_ID));
        BundleHelper.registerBundle(bundle, connection);

        // add to default/user views
        final List<Long> views = FlywayHelper.getUserAndDefaultViewIds(connection);
        for(Long viewId : views){
            if (FlywayHelper.viewContainsBundle(connection, BUNDLE_ID, viewId)) {
                continue;
            }
            FlywayHelper.addBundleWithDefaults(connection, viewId, BUNDLE_ID);
        }
    }
}
