package flyway.paikkis;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by SMAKINEN on 26.5.2017.
 */
public class V2_5__Update_promote_bundle_urls implements JdbcMigration {

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
        final String sql = "SELECT view_id, bundle_id, config, bundleinstance FROM portti_view_bundle_seq " +
                "WHERE bundle_id = (select id from portti_bundle where name = '" + bundle + "')";
        try (final PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while(rs.next()) {
                Bundle b = new Bundle();
                b.setViewId(rs.getLong("view_id"));
                b.setBundleId(rs.getLong("bundle_id"));
                b.setConfig(rs.getString("config"));
                b.setBundleinstance(rs.getString("bundleinstance"));
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
                " AND bundleinstance=?" +
                " AND view_id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, bundle.getConfig());
            statement.setLong(2, bundle.getBundleId());
            statement.setString(3, bundle.getBundleinstance());
            statement.setLong(4, bundle.getViewId());
            statement.execute();
        }
        return null;
    }

    public String getBundle() {
        return "promote";
    }

    public String getModifiedConfig(final String param) throws Exception {
        final JSONObject config = JSONHelper.createJSONObject(param);
        JSONObject login = new JSONObject();
        JSONHelper.putValue(login, "fi", "/auth?lang=fi");
        JSONHelper.putValue(login, "sv", "/auth?lang=sv");
        JSONHelper.putValue(login, "en", "/auth?lang=en");
        JSONHelper.putValue(config, "signupUrl", login);

        JSONObject register = new JSONObject();
        JSONHelper.putValue(register, "fi", "https://omatili.maanmittauslaitos.fi/?lang=fi");
        JSONHelper.putValue(register, "sv", "https://omatili.maanmittauslaitos.fi/?lang=sv");
        JSONHelper.putValue(register, "en", "https://omatili.maanmittauslaitos.fi/?lang=en");
        JSONHelper.putValue(config, "registerUrl", register);
        return config.toString(2);
    }
}