package flyway.olcesium;

import fi.nls.oskari.db.ViewHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONObject;

import java.sql.Connection;

public class V1_0__add_development_olcesium_view implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_0__add_development_olcesium_view.class);
    private ViewService service = null;

    public void migrate(Connection connection) throws Exception {
        service = new ViewServiceIbatisImpl();

        final String file = PropertyUtil.get("flyway.olcesium.1_0.file", "paikkis-olcesium-dev.json");
        // configure the view that should be used as default options
        final int defaultViewId = PropertyUtil.getOptional("flyway.olcesium.1_0.view", (int) service.getDefaultViewId());
        try {
            // load view from json and update startups for bundles
            JSONObject json = ViewHelper.readViewFile(file);
            View view = ViewHelper.createView(json);

            View defaultView = service.getViewWithConf(defaultViewId);
            for(Bundle bundle: defaultView.getBundles()) {
                Bundle newBundle = view.getBundleByName(bundle.getName());
                if(newBundle == null) {
                    continue;
                }
                // copy the settings (state and config) from current default view
                // newBundle.setState(bundle.getState());
                // newBundle.setConfig(bundle.getConfig());
            }
            // save to db
            service.addView(view);
            LOG.info("Geoportal view with Ol Cesium added with uuid", view.getUuid());
        } catch (Exception e) {
            LOG.warn(e, "Something went wrong while inserting the view!",
                    "The update failed so to have an olcesium view you need to remove this update from the database table oskari_status_paikkis, " +
                            "tune the template file:", file, " and restart the server to try again");
            throw e;
        }
    }
}