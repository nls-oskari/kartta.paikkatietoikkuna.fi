package flyway.pti;

import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppSetupHelper;
import org.oskari.helpers.BundleHelper;

import java.sql.Connection;
import java.util.List;

/**
 * Adds layer analytics bundle to embedded map views
 */

public class V3_27_2__add_layeranalytics_bundle extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {

        Connection connection = context.getConnection();
        String bundleName = "layeranalytics";
        Bundle layerAnalyticsBundle = new Bundle(bundleName);
        List<Long> appsetupIds = AppSetupHelper.getSetupsForType(connection, null);
        AppSetupHelper.addOrUpdateBundleInApps(connection, layerAnalyticsBundle, appsetupIds);
    }
}
