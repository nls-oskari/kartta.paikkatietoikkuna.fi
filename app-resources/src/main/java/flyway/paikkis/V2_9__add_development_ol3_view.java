package flyway.paikkis;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppSetupHelper;


public class V2_9__add_development_ol3_view extends BaseJavaMigration {

    private static final Logger LOG = LogFactory.getLogger(V2_9__add_development_ol3_view.class);
    private ViewService service = null;

    public void migrate(Context context) throws Exception {
        service = new AppSetupServiceMybatisImpl();

        final String file = PropertyUtil.get("flyway.paikkis.2_9.file", "paikkis-ol3-dev.json");
        // configure the view that should be used as default options
        final int defaultViewId = PropertyUtil.getOptional("flyway.paikkis.2_9.view", (int) service.getDefaultViewId());
        try {
            // load view from json and update startups for bundles
            long appsetupId = AppSetupHelper.create(context.getConnection(), "paikkis-ol3-dev.json");
            View view = service.getViewWithConf(appsetupId);

            View defaultView = service.getViewWithConf(defaultViewId);
            for(Bundle bundle: defaultView.getBundles()) {
                Bundle newBundle = view.getBundleByName(bundle.getName());
                if(newBundle == null) {
                    continue;
                }
                // copy the settings (state and config) from current default view
                newBundle.setState(bundle.getState());
                newBundle.setConfig(bundle.getConfig());
            }
            // save to db
            service.addView(view);
            LOG.info("Geoportal view with Openlayers 3 added with uuid", view.getUuid());
        } catch (Exception e) {
            LOG.warn(e, "Something went wrong while inserting the view!",
                    "The update failed so to have an ol3 view you need to remove this update from the database table oskari_status_paikkis, " +
                            "tune the template file:", file, " and restart the server to try again");
            throw e;
        }
    }
}