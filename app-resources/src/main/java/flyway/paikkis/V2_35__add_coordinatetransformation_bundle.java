package flyway.paikkis;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppSetupHelper;

import java.sql.Connection;
import java.util.List;

/**
 * Registers and adds 'coordinatetransformation' bundle to default and user views.
 */
public class V2_35__add_coordinatetransformation_bundle extends BaseJavaMigration {

    private static final Logger LOG = LogFactory.getLogger(V2_35__add_coordinatetransformation_bundle.class);
    private static final String BUNDLE_ID = "coordinatetransformation";

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
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
