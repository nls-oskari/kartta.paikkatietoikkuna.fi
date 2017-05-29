package flyway.ptimigration;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.view.modifier.ViewModifier;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by SMAKINEN on 26.5.2017.
 */
public abstract class ConfigMigration implements JdbcMigration {

    public void migrate(Connection connection)
            throws Exception {
        final ArrayList<Bundle> bundles = getBundles(connection, getBundle());
        for (Bundle bundle : bundles) {
            final String config = getModifiedConfig(bundle.getConfig());
            if (config == null) {
                continue;
            }
            bundle.setConfig(config);
            // update view back to db
            updateBundleInView(connection, bundle);
        }
    }

    private ArrayList<Bundle> getBundles(Connection connection, String bundle) throws Exception {
        ArrayList<Bundle> ids = new ArrayList<>();
        final String sql = "SELECT view_id, bundle_id, config FROM portti_view_bundle_seq " +
                "WHERE bundle_id = (select id from portti_bundle where name = '" + bundle + "')";
        try (final PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while(rs.next()) {
                Bundle b = new Bundle();
                b.setViewId(rs.getLong("view_id"));
                b.setBundleId(rs.getLong("bundle_id"));
                b.setConfig(rs.getString("config"));
                ids.add(b);
            }
        }
        return ids;
    }

    public static Bundle updateBundleInView(Connection connection, Bundle bundle)
            throws SQLException {
        final String sql = "UPDATE portti_view_bundle_seq SET " +
                "config=? " +
                " WHERE bundle_id=? " +
                " AND view_id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, bundle.getConfig());
            statement.setLong(2, bundle.getBundleId());
            statement.setLong(3, bundle.getViewId());
            statement.execute();
        }
        return null;
    }

    public String getBundle() {
        return ViewModifier.BUNDLE_MAPFULL;
    }
    protected abstract String getModifiedConfig(String config) throws Exception;
}
