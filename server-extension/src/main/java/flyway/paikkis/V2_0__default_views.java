package flyway.paikkis;

import fi.nls.oskari.db.LayerHelper;
import fi.nls.oskari.db.ViewHelper;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONObject;

import java.sql.Connection;

/**
 * Creates a new view that can be used to start developing a new Openlayers 3 based geoportal view.
 * Not for production use
 */
public class V2_0__default_views implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V2_0__default_views.class);

    private String[] viewFiles = {"paikkis-user-view.json", "paikkis-guest-view.json", "paikkis-publish-template.json"};
    private String[] layerFiles = {"maastokartta.json", "ortokuva.json", "taustakartta.json"};

    public void migrate(Connection connection) throws Exception {
        if(PropertyUtil.getOptional("flyway.paikkis.2_0.skip", false)) {
            return;
        }
        for(String file : viewFiles) {
            long id = ViewHelper.insertView(connection, file);
            LOG.info("View inserted from", file, "with id:", id);
        }
        for(String file : layerFiles) {
            long id = LayerHelper.setupLayer(file);
            LOG.info("Layer inserted from", file, "with id:", id);
        }

    }
}