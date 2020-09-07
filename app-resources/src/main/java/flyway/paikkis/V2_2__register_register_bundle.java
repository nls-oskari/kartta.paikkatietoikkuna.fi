package flyway.paikkis;

import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.BundleHelper;

import java.sql.Connection;

/**
 * Checks if bundle is already present in the db and inserts it if not
 */
public class V2_2__register_register_bundle extends BaseJavaMigration {

    private static final String NAME = "register";
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        // BundleHelper checks if these bundles are already registered
        Bundle bundle = new Bundle();
        bundle.setName(NAME);
        BundleHelper.registerBundle(bundle, connection);
    }
}
