package flyway.olcesium;

import fi.nls.oskari.db.BundleHelper;
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

public class V1_7__3D_view_link implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_7__3D_view_link.class);
    private static final String CESIUM_VIEW_NAME = "Geoportal Ol Cesium";
    private static final String BUNDLE_NAME = "demo-link";
    private static final String BUNDLE_PATH = "/Oskari/packages/paikkatietoikkuna/bundle/";

    public void migrate(Connection connection) {
        if (reqisterLinkBundle(connection)) {
            Long viewId = getDefaultViewId(connection);
            if (viewId != null) {
                try {
                    if (!FlywayHelper.viewContainsBundle(connection, BUNDLE_NAME, viewId)) {
                        FlywayHelper.addBundleWithDefaults(connection, viewId, BUNDLE_NAME);
                    }
                }
                catch (SQLException e) {
                    LOG.error(e, "Error adding link bundle to default view");
                }
            }
        }
    }

    private boolean reqisterLinkBundle(Connection connection) {
        try {
            String uuid = getCesiumViewUuid(connection);
            if (uuid != null) {
                // register bundle
                Bundle bundle = new Bundle();
                bundle.setName(BUNDLE_NAME);
                bundle.setStartup(BundleHelper.getBundleStartup(BUNDLE_PATH, BUNDLE_NAME, BUNDLE_NAME));
                bundle.setConfig(JSONHelper.createJSONObject("uuid", uuid).toString());
                BundleHelper.registerBundle(bundle, connection);
                return true;
            }
        }
        catch (Exception ex) {
            LOG.error(ex, "Error registering the link bundle");
        }
        return false;
    }

    private String getCesiumViewUuid(Connection conn) {
        final String sql = "SELECT uuid FROM portti_view WHERE name=? order by usagecount desc limit 1";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, CESIUM_VIEW_NAME);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    return rs.getString("uuid");
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Error getting Cesium portti view uuid");
        }
        return null;
    }

    private Long getDefaultViewId(Connection conn) {
        final String sql = "SELECT id FROM portti_view WHERE type='DEFAULT' order by usagecount desc limit 1";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Error getting default portti view id");
        }
        return null;
    }

}
