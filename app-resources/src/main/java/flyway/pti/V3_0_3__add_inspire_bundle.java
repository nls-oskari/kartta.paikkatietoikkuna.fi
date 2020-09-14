package flyway.pti;

import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppSetupHelper;
import org.oskari.helpers.BundleHelper;

import java.sql.Connection;

/**
 * Adds inspire info bundle to all default/user appsetups
 */
public class V3_0_3__add_inspire_bundle extends BaseJavaMigration {

    public void migrate(Context context) throws Exception {
        String bundleID = "inspire";
        Connection connection = context.getConnection();
        Bundle bundle = new Bundle(bundleID);
        BundleHelper.registerBundle(bundle, connection);
        AppSetupHelper.addBundleToDefaultAndUserApps(connection, bundle);
    }
}
