package flyway.pti3d;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.db.DBHandler;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

/**
 * Creates a new 3D view
 */
public class V1_0__create_3D_view implements JdbcMigration {

    public void migrate(Connection connection) throws Exception {
        // ensure we have the bundle
        Bundle b  = new Bundle();
        b.setName("dimension-change");
        BundleHelper.registerBundle(b, connection);

        DBHandler.setupAppContent(connection, "paikkis-3D.json");
    }
}