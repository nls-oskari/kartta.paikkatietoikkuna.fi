package flyway.pti;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class V3_27_4__replace_user_role_with_karttakayttajat extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(V3_27_4__replace_user_role_with_karttakayttajat.class);

    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        addKarttakayttajatRoleForUsersWithUserRole(conn);
        deleteUserRoleAssignments(conn);
        deleteUserRole(conn);
    }


    private void addKarttakayttajatRoleForUsersWithUserRole(Connection conn) throws SQLException {
        Long userRoleId = getRoleId(conn, "User");
        Long karttakayttajatRoleId = getRoleId(conn, "Karttakäyttäjät");
        if (userRoleId == null || karttakayttajatRoleId == null) {
            return;
        }

        Set<Long> userRoleUsers = getUserIdsForRole(conn, userRoleId);
        Set<Long> karttakayttajatUsers = getUserIdsForRole(conn, karttakayttajatRoleId);
        userRoleUsers.removeAll(karttakayttajatUsers);
        if (userRoleUsers.isEmpty()) {
            return;
        }

        int insertedRows = insertUserRoleAssignments(conn, userRoleUsers, karttakayttajatRoleId);
        LOG.info("Inserted " + insertedRows + " rows into oskari_users_roles");
    }

    private Long getRoleId(Connection conn, String roleName) throws SQLException {
        final String sql = "SELECT id FROM oskari_roles WHERE name = ?";
        try (final PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, roleName);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                long roleId = rs.getLong(1);
                return rs.wasNull() ? null : roleId;
            }
        }
    }

    private Set<Long> getUserIdsForRole(Connection conn, long roleId) throws SQLException {
        final String sql = "SELECT user_id FROM oskari_users_roles WHERE role_id = ?";
        Set<Long> userIds = new HashSet<>();
        try (final PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, roleId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getLong(1));
                }
            }
        }
        return userIds;
    }

    private int insertUserRoleAssignments(Connection conn, Set<Long> userIds, long roleId) throws SQLException {
        final String sql = "INSERT INTO oskari_users_roles (user_id, role_id) VALUES (?, ?)";
        try (final PreparedStatement statement = conn.prepareStatement(sql)) {
            for (Long userId : userIds) {
                statement.setLong(1, userId);
                statement.setLong(2, roleId);
                statement.addBatch();
            }
            int[] rows = statement.executeBatch();
            int insertedRows = 0;
            for (int rowCount : rows) {
                if (rowCount > 0) {
                    insertedRows += rowCount;
                }
            }
            return insertedRows;
        }
    }

    private void deleteUserRoleAssignments(Connection conn) throws SQLException {
        final String sql = "DELETE FROM oskari_users_roles "
                + "WHERE role_id = (SELECT id FROM oskari_roles WHERE name = 'User')";
        try (final PreparedStatement statement = conn.prepareStatement(sql)) {
            int deletedRows = statement.executeUpdate();
            LOG.info("Deleted " + deletedRows + " rows from oskari_users_roles");
        }
    }

    private void deleteUserRole(Connection conn) throws SQLException {
        final String sql = "DELETE FROM oskari_roles WHERE name = 'User'";
        try (final PreparedStatement statement = conn.prepareStatement(sql)) {
            int deletedRows = statement.executeUpdate();
            LOG.info("Deleted " + deletedRows + " rows from oskari_roles");
        }
    }
}

