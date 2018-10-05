package flyway.olcesium;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.FlywayHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class V1_8__2D_view_link implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_8__2D_view_link.class);
    private static final String CESIUM_VIEW_NAME = "Geoportal Ol Cesium";
    private static final String BUNDLE_NAME = "demo-link";
    private static final Integer LOW_STARTUP_PRIO = 1000;
    private static final String EMPTY_CONFIG = "{}";
    private ViewService viewService = null;

    public void migrate(Connection connection) {
        viewService =  new ViewServiceIbatisImpl();
        updateCesiumViews(connection);
    }

    private void updateCesiumViews(Connection connection) {
        try {
            List<Long> viewIds = getCesiumViewIds(connection);
            for(Long viewId : viewIds) {
                try {
                    if (!FlywayHelper.viewContainsBundle(connection, BUNDLE_NAME, viewId)) {
                        this.addBundle(connection, viewId);
                    }
                }
                catch (SQLException e) {
                    LOG.error(e, "Error adding link bundle to 3D view");
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Error setting link bundle for 3D views");
        }
    }

    private List<Long> getCesiumViewIds(Connection conn) {
        List<Long> list = new ArrayList<>();
        final String sql = "SELECT id FROM portti_view WHERE name=?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, CESIUM_VIEW_NAME);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getLong("id"));
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Error getting Cesium portti views");
        }
        return list;
    }

    private static void addBundle(Connection connection, Long viewId) throws SQLException {
        final String sql ="INSERT INTO portti_view_bundle_seq" +
                "(view_id, bundle_id, seqno, config, state, startup, bundleinstance) " +
                "VALUES (" +
                "?, " +
                "(SELECT id FROM portti_bundle WHERE name=?), " +
                "?, " +
                "?, " +
                "(SELECT state FROM portti_bundle WHERE name=?),  " +
                "(SELECT startup FROM portti_bundle WHERE name=?), " +
                "?)";
        try(final PreparedStatement statement =
                    connection.prepareStatement(sql)) {
            statement.setLong(1, viewId);
            statement.setString(2, BUNDLE_NAME);
            statement.setInt(3, LOW_STARTUP_PRIO);
            statement.setString(4, EMPTY_CONFIG);
            statement.setString(5, BUNDLE_NAME);
            statement.setString(6, BUNDLE_NAME);
            statement.setString(7, BUNDLE_NAME);
            statement.execute();
        }
    }


}
