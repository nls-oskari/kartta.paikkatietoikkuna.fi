package flyway.paikkis;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MKUOSMANEN on 14.6.2017.
 */
public class V2_4__fix_coordinatetool_decimals_for_3067_projection extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(V2_4__fix_coordinatetool_decimals_for_3067_projection.class);
    private static final String BUNDLE_COORDINATETOOL = "coordinatetool";
    private static final String PROJECTION_SHOW_FORMAT = "projectionShowFormat";
    private static final String PROJECTION_EUREF_FIN = "EPSG:3067";
    private static final String DECIMALS = "decimals";


    private ViewService service = null;
    private int updatedViewCount = 0;

    public void migrate(Context context) throws Exception {
        service =  new AppSetupServiceMybatisImpl();
        try {
            updateViews(context.getConnection());
        }
        finally {
            LOG.info("Updated views:", updatedViewCount);
            service = null;
        }
    }

    private void updateViews(Connection conn)
            throws Exception {
        List<View> list = getViews(conn);
        for(View view : list) {
            View modifyView = service.getViewWithConf(view.getId());

            final Bundle coordinatetool = modifyView.getBundleByName(BUNDLE_COORDINATETOOL);
            boolean modified = modifyProjectionDecimals(coordinatetool);
            if(modified) {
                service.updateBundleSettingsForView(view.getId(), coordinatetool);
                updatedViewCount++;
            }
        }
    }

    private List<View> getViews(Connection conn) throws SQLException {
        List<View> list = new ArrayList<>();
        final String sql = "SELECT view_id " +
                "FROM portti_view_bundle_seq " +
                "WHERE bundle_id IN (SELECT id FROM portti_bundle WHERE name = '"+BUNDLE_COORDINATETOOL+"');";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    View view = new View();
                    view.setId(rs.getLong("view_id"));
                    list.add(view);
                }
            }
        }
        return list;
    }

    private boolean modifyProjectionDecimals(final Bundle coordinatetool) throws JSONException {
        final JSONObject config = coordinatetool.getConfigJSON();
        final JSONObject defaultEurefProjectionConfig = new JSONObject("{\"decimals\": 0}");
        final JSONObject defaultProjectionConfig = new JSONObject("{\"EPSG:3067\":{\"decimals\": 0}, \"decimals\": 3, \"format\":\"metric\"}");
        if(config.has(PROJECTION_SHOW_FORMAT)) {
            final JSONObject projectionShowFormat = config.getJSONObject(PROJECTION_SHOW_FORMAT);
            if(projectionShowFormat.has(PROJECTION_EUREF_FIN)) {
                final JSONObject eurefJSON = projectionShowFormat.getJSONObject(PROJECTION_EUREF_FIN);
                if(eurefJSON.has(DECIMALS) && 0 != eurefJSON.getInt(DECIMALS)) {
                    eurefJSON.remove(DECIMALS);
                    eurefJSON.put(DECIMALS, 0);
                    return true;
                } else {
                    eurefJSON.put(DECIMALS, 0);
                    return true;
                }
            }
            else {
                projectionShowFormat.put(PROJECTION_EUREF_FIN, defaultEurefProjectionConfig);
                return true;
            }
        }
        else {
            config.put(PROJECTION_SHOW_FORMAT, defaultProjectionConfig);
            return true;
        }
    }
}
