package flyway.paikkis;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Checks if bundle is already present in the db and inserts it if not
 */
public class V2_2__register_register_bundle implements JdbcMigration {

    private static final String NAME = "register";
    public void migrate(Connection connection) throws SQLException {
        // BundleHelper checks if these bundles are already registered
        Bundle bundle = new Bundle();
        bundle.setName(NAME);
        bundle.setStartup(BundleHelper.getBundleStartup("/Oskari/packages/paikkatietoikkuna/bundle/", NAME, "register"));
        BundleHelper.registerBundle(bundle, connection);
    }
}
