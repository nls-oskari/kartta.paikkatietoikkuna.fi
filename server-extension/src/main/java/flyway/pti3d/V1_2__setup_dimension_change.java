package flyway.pti3d;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.FlywayHelper;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class V1_2__setup_dimension_change implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_1__set_backgroundlayers.class);
    private static final String APPLICATION_3D_NAME = "full-map-3D";
    private static final String DEFAULT_VIEW_TYPE = "DEFAULT";
    private static final String BUNDLE_NAME = "dimension-change";

    public void migrate(Connection connection) throws SQLException {
        String uuid = get3DViewUuid(connection);
        List<Long> viewIds = FlywayHelper.getUserAndDefaultViewIds(connection);

        for (Long id : viewIds) {
            if (!FlywayHelper.viewContainsBundle(connection, BUNDLE_NAME, id)) {
                FlywayHelper.addBundleWithDefaults(connection, id, BUNDLE_NAME);
                Bundle bundle = FlywayHelper.getBundleFromView(connection, BUNDLE_NAME, id);
                bundle.setConfig(JSONHelper.createJSONObject("uuid", uuid).toString());
                FlywayHelper.updateBundleInView(connection, bundle, id);
            }
        }
    }

    private String get3DViewUuid(Connection conn) {
        final String sql = "SELECT uuid FROM portti_view WHERE application=? and type=? limit 1";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, APPLICATION_3D_NAME);
            statement.setString(2, DEFAULT_VIEW_TYPE);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    return rs.getString("uuid");
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Error getting 3D portti view uuid");
        }
        return null;
    }
}
