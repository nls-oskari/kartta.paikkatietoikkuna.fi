package flyway.pti3d;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewException;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.util.JSONHelper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

public class V1_5__add_3857_to_coordinatetool implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_5__add_3857_to_coordinatetool.class);
    private static final String BUNDLE_NAME = "coordinatetool";
    private static final String APPLICATION_3D_FULL = "full-map-3D";
    private static final String APPLICATION_3D_PUBLISHED = "published-map-3D";
    private static final String SUPPORTED_PROJECTIONS = "supportedProjections";
    private static final String EPSG_3857 = "EPSG:3857";
    private ViewService viewService = null;

    public void migrate(Connection connection) throws SQLException, ViewException, JSONException {
        viewService =  new AppSetupServiceMybatisImpl();
        updateDefaultAndUserViews(connection);
    }

    private void updateDefaultAndUserViews(Connection connection) throws SQLException, ViewException, JSONException {
        List<Long> viewIds = get3DApplicationViews(connection);
        JSONObject format = JSONHelper.createJSONObject("decimals", 0);
        for(Long viewId : viewIds) {
            View modifyView = viewService.getViewWithConf(viewId);
            Bundle bundle = modifyView.getBundleByName(BUNDLE_NAME);
            if (bundle == null) {
                // published maps may not have coordinatetool plugin
                continue;
            }
            JSONObject config = JSONHelper.createJSONObject(bundle.getConfig());
            if (config == null) {
                LOG.warn("Adding 3857 to coordinatetool. Coordinatetool config not found for view: ", viewId);
                continue;
            }
            JSONArray projections = config.optJSONArray(SUPPORTED_PROJECTIONS);
            if (projections == null) {
                // published maps may not have transform functionality, skip
                continue;
            }
            // Add 3857 to supported projections
            JSONArray projectionsWith3857 = new JSONArray();
            projectionsWith3857.put(EPSG_3857);
            for (int i = 0; i < projections.length() ; i++ ) {
                projectionsWith3857.put(projections.getString(i));
            }
            JSONHelper.put(config, SUPPORTED_PROJECTIONS, projectionsWith3857);
            // Use 0 decimals like with 3067
            JSONObject showFormat = config.optJSONObject("projectionShowFormat");
            if (showFormat != null) {
                JSONHelper.putValue(showFormat, EPSG_3857, format);
            }
            bundle.setConfig(config.toString());
            viewService.updateBundleSettingsForView(viewId, bundle);
        }
    }
    private List<Long> get3DApplicationViews(Connection conn) throws SQLException {
        List<Long> list = new ArrayList<>();
        final String sql = "SELECT id FROM portti_view WHERE application=? OR application=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, APPLICATION_3D_FULL);
            statement.setString(2, APPLICATION_3D_PUBLISHED);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getLong("id"));
                }
            }
        }
        return list;
    }
}
