package flyway.paikkis;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppSetupHelper;
import org.oskari.helpers.LayerHelper;

import java.sql.Connection;

/**
 * Creates a new view that can be used to start developing a new Openlayers 3 based geoportal view.
 * Not for production use
 */
public class V2_0__default_views extends BaseJavaMigration {

    private static final Logger LOG = LogFactory.getLogger(V2_0__default_views.class);

    private String[] viewFiles = {"geoportal.json", "publish-template.json"};
    private String[] layerFiles = {"maastokartta.json", "ortokuva.json", "taustakartta.json"};

    public void migrate(Context context) throws Exception {
        if(PropertyUtil.getOptional("flyway.paikkis.2_0.skip", false)) {
            return;
        }

        Connection connection = context.getConnection();
        for(String file : viewFiles) {
            long id = AppSetupHelper.create(connection, file);
            LOG.info("Appsetup inserted from", file, "with id:", id);
        }

        for(String file : layerFiles) {
            long id = LayerHelper.setupLayer(file);
            LOG.info("Layer inserted from", file, "with id:", id);
        }

    }
}