package flyway.pti;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppSetupHelper;

import java.sql.Connection;
import java.util.List;

/**
 * Remove coordinatetransformation from default and user 3D appsetups
 */
public class V3_0_5__remove_coordtrans_from_3d extends BaseJavaMigration {
    private Logger log = LogFactory.getLogger(V3_0_5__remove_coordtrans_from_3d.class);
    private static final String APP_3D = "geoportal-3D";
    private static final String BUNDLE = "coordinatetransformation";

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        List<Long> apps =  AppSetupHelper.getSetupsForUserAndDefaultType(connection, APP_3D);
        for (Long id : apps) {
            AppSetupHelper.removeBundleFromApp(connection, id, BUNDLE);
        }
        log.info("Removed coordinatetransformation bundle from:", apps.size(), "appsetups");
    }
}
