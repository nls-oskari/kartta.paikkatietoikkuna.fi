package flyway.paikkis;

import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppSetupHelper;
import org.oskari.helpers.BundleHelper;

import java.sql.Connection;
import java.util.List;

/**
 * Registers and adds 'telemetry' bundle to default and user views.
 */
public class V2_27__add_telemetry_to_default_views extends BaseJavaMigration {
    private static final String BUNDLE_ID = "telemetry";

    public void migrate(Context context) throws Exception {

        Connection connection = context.getConnection();
        // register bundle
        Bundle bundle = new Bundle();
        bundle.setName(BUNDLE_ID);
        BundleHelper.registerBundle(bundle, connection);

        // add to default/user views
        final List<Long> views = AppSetupHelper.getSetupsForUserAndDefaultType(connection);
        for(Long viewId : views){
            if (AppSetupHelper.appContainsBundle(connection, viewId, BUNDLE_ID)) {
                continue;
            }
            AppSetupHelper.addBundleToApp(connection, viewId, BUNDLE_ID);
        }
    }
}
