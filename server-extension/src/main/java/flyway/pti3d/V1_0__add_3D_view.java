package flyway.pti3d;

import fi.nls.oskari.db.ViewHelper;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONObject;

import java.sql.Connection;

public class V1_0__add_3D_view implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(flyway.pti3d.V1_0__add_3D_view.class);
    private ViewService service = null;

    public void migrate(Connection connection) throws Exception {
        service = new AppSetupServiceMybatisImpl();

        final String file = PropertyUtil.get("flyway.pti3D.1_0.file", "pti-3D.json");
        try {
            // load view from json and update startups for bundles
            JSONObject json = ViewHelper.readViewFile(file);
            View view = ViewHelper.createView(json);
            // save to db
            service.addView(view);
            LOG.info("3D view added with uuid", view.getUuid());
        } catch (Exception e) {
            LOG.warn(e, "Something went wrong while inserting the view!",
                    "The update failed so to have an 3D view you need to remove this update from the database table oskari_status_pti3D, " +
                        "tune the template file:", file, " and restart the server to try again");
            throw e;
        }
    }
}
