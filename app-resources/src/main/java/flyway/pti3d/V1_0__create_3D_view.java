package flyway.pti3d;

import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppSetupHelper;
import org.oskari.helpers.BundleHelper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Creates a new 3D view
 */
public class V1_0__create_3D_view extends BaseJavaMigration {

    public void migrate(Context context) throws SQLException, IOException {
        Connection connection = context.getConnection();
        // ensure we have the bundle
        Bundle b  = new Bundle();
        b.setName("dimension-change");
        BundleHelper.registerBundle(b, connection);

        AppSetupHelper.create(connection, "geoportal-3D.json");
    }
}