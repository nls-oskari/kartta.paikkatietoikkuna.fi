package flyway.pti;

import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppSetupHelper;

import java.sql.Connection;

/**
 * Adds layerswipe bundle to all default/user 2D appsetups
 */
public class V3_0_6__add_layerswipe_bundle extends BaseJavaMigration {

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        // add layerswipe but only to 2D geoportal views
        AppSetupHelper.addBundleToApps(connection,
                new Bundle("layerswipe"), "geoportal");
    }
}
