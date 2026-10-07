package flyway.pti;

import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppSetupHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Remove myplaces & userlayer bundles, add myfeatures bundle to where myplaces was
 */
public class V3_34_2__switch_userdata_bundles extends BaseJavaMigration {
    private static final String BUNDLE_MYPLACES = "myplaces3";
    private static final String BUNDLE_USERLAYER = "myplacesimport";
    private static final String BUNDLE_MYFEATURES = "myfeatures";


    public void migrate(Context context) throws Exception {

        Connection connection = context.getConnection();
        // migrate all appsetups
        List<Long> viewIds =  AppSetupHelper.getSetupsForUserAndDefaultType(connection);
        for (Long id : viewIds) {
            updateAppsetup(connection, id);
        }
    }

    private void updateAppsetup(Connection connection, Long viewId) throws SQLException {
        Bundle myplacesBundle =  AppSetupHelper.getAppBundle(connection, viewId, BUNDLE_MYPLACES);
        if (myplacesBundle == null) {
            return;
        }
        // remove old bundles
        AppSetupHelper.removeBundleFromApp(connection, viewId, BUNDLE_MYPLACES);
        AppSetupHelper.removeBundleFromApp(connection, viewId, BUNDLE_USERLAYER);

        // add myfeatures bundle
        Bundle myfeatures = new Bundle(BUNDLE_MYFEATURES);
        // switch with myplaces
        myfeatures.setSeqNo(myplacesBundle.getSeqNo());
        AppSetupHelper.addOrUpdateBundleInApps(connection, myfeatures, List.of(viewId));

    }
}
