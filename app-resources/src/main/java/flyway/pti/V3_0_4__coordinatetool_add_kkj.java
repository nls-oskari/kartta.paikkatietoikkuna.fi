package flyway.pti;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.helpers.AppSetupHelper;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Adds NLSFI:kkj to coordinatetool supported projections to all default/user appsetups
 */
public class V3_0_4__coordinatetool_add_kkj extends BaseJavaMigration {
    private static final String KKJ = "NLSFI:kkj";
    private static final String ADD_BEFORE = "NLSFI:ykj";
    private static final String BUNDLE = "coordinatetool";
    private static final String SUPPORTED_EPSG = "supportedProjections";

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        // embedded maps doesn't have transformations, migrate only default and user appsetups
        List<Long> viewIds =  AppSetupHelper.getSetupsForUserAndDefaultType(connection);
        for (Long id : viewIds) {
            updateAppsetup(connection, id);
        }
    }
    private void updateAppsetup(Connection connection, Long viewId) throws SQLException {
        Bundle bundle =  AppSetupHelper.getAppBundle(connection, viewId, BUNDLE);
        if (bundle == null) {
            //skip
            return;
        }
        JSONObject conf = bundle.getConfigJSON();
        List<String> epsgs = JSONHelper.getArrayAsList(conf.optJSONArray(SUPPORTED_EPSG));
        if (epsgs.isEmpty() || epsgs.contains(KKJ)) {
            // skip
            return;
        }
        int index = epsgs.indexOf(ADD_BEFORE);
        if (index != -1) {
            epsgs.add(index, KKJ);
        } else {
            epsgs.add(KKJ);
        }
        JSONHelper.put(conf, SUPPORTED_EPSG, new JSONArray(epsgs));
        bundle.setConfig(conf.toString());
        AppSetupHelper.updateAppBundle(connection, viewId, bundle);
    }
}
