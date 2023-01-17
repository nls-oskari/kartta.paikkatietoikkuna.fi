package flyway.pti;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;
import org.oskari.helpers.AppSetupHelper;

import java.sql.Connection;
import java.util.List;

/**
 * Change reverse geocoding channel for testing purposes on coordinatetool for all appsetups.
 *
 * This is an extendable migration that will later be used to remove reverse geocoding from coordinatetool.
 * The feature isn't used in PTI currently and there are some configs on the database that can be cleaned.
 */
public class V3_2_0__coordinatetool_remove_reverse_geocode extends BaseJavaMigration {
    private static final String BUNDLE = "coordinatetool";

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        List<Long> viewIds =  AppSetupHelper.getSetupsForType(connection);
        for (Long id : viewIds) {
            Bundle bundle = AppSetupHelper.getAppBundle(connection, id, BUNDLE);
            if (updateBundle(bundle)) {
                AppSetupHelper.updateAppBundle(connection, id, bundle);
            }
        }
    }
    protected boolean updateBundle(Bundle bundle) {
        if (bundle == null) {
            return false;
        }
        JSONObject conf = bundle.getConfigJSON();
        // This is just for testing:
        JSONHelper.putValue(conf, "reverseGeocodingIds", "TM35LEHTIJAKO_CHANNEL");
        bundle.setConfig(conf.toString());
        return true;
    }
    /*
    protected boolean updateBundle(Bundle bundle) {
        if (bundle == null) {
            return false;
        }
        // This will be the migration to run once testing is complete:
        JSONObject conf = bundle.getConfigJSON();
        boolean updateRequired = conf.remove("isReverseGeocode") != null;
        updateRequired = updateRequired || conf.remove("reverseGeocodingIds") != null;
        bundle.setConfig(conf.toString());
        return updateRequired;
    }
     */
}
