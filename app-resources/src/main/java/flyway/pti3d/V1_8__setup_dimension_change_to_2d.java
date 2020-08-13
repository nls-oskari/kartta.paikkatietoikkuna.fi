package flyway.pti3d;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.util.FlywayHelper;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class V1_8__setup_dimension_change_to_2d implements JdbcMigration {

    private static final String BUNDLE_NAME = "dimension-change";
    private static final String APPLICATION_2D_NAME = "geoportal";
    private static final String APPLICATION_3D_NAME = "geoportal-3D";

    public void migrate(Connection connection) throws SQLException {
        String uuid = FlywayHelper.getDefaultViewUuid(connection, APPLICATION_2D_NAME);
        final List<Long> viewIds = FlywayHelper.getUserAndDefaultViewIds(connection, APPLICATION_3D_NAME);

        for (Long id : viewIds) {
            if (!FlywayHelper.viewContainsBundle(connection, BUNDLE_NAME, id)) {
                FlywayHelper.addBundleWithDefaults(connection, id, BUNDLE_NAME);
            }
            Bundle bundle = FlywayHelper.getBundleFromView(connection, BUNDLE_NAME, id);
            bundle.setConfig(JSONHelper.createJSONObject("uuid", uuid).toString());
            FlywayHelper.updateBundleInView(connection, bundle, id);
        }
    }
}
