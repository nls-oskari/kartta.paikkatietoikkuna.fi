package flyway.pti;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class V3_27_3__delete_power_user_role extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(V3_27_3__delete_power_user_role.class);

    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        deletePowerUserRoleAssignments(conn);
        deletePowerUserRole(conn);
    }

    private void deletePowerUserRoleAssignments(Connection conn) throws SQLException {
        final String sql = "DELETE FROM oskari_users_roles WHERE role_id = "
                + "(SELECT id FROM oskari_roles WHERE name = 'Power User')";
        try (final PreparedStatement statement = conn.prepareStatement(sql)) {
            int deletedRows = statement.executeUpdate();
            LOG.info("Deleted " + deletedRows + " rows from oskari_users_roles");
        }
    }

    private void deletePowerUserRole(Connection conn) throws SQLException {
        final String sql = "DELETE FROM oskari_roles WHERE name = 'Power User'";
        try (final PreparedStatement statement = conn.prepareStatement(sql)) {
            int deletedRows = statement.executeUpdate();
            LOG.info("Deleted " + deletedRows + " rows from oskari_roles");
        }
    }
}

