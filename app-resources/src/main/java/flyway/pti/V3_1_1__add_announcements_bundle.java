package flyway.pti;

import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppSetupHelper;

import java.sql.Connection;

/**
 * Adds announcements bundle to all default/user appsetups
 */
public class V3_1_1__add_announcements_bundle extends BaseJavaMigration {
    private static final String BUNDLE_NAME = "announcements";
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        AppSetupHelper.addBundleToApps(connection, BUNDLE_NAME);
    }
}
