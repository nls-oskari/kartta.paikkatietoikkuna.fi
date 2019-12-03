package flyway.pti3d;

import fi.nls.oskari.db.DBHandler;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

/**
 * Creates a new 3D view
 */
public class V1_0__create_3D_view implements JdbcMigration {

    public void migrate(Connection connection) throws Exception {
        DBHandler.setupAppContent(connection, "paikkis-3D.json");
    }
}