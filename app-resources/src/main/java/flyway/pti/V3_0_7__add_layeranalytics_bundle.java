package flyway.pti;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.BundleHelper;

import java.sql.Connection;

/**
 * Register admin-layeranalytics bundle so we can configure it for admins
 */
public class V3_0_7__add_layeranalytics_bundle extends BaseJavaMigration {

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        BundleHelper.registerBundle(connection, "admin-layeranalytics");
    }
}
