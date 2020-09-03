package flyway.pti3d;

import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONException;
import org.oskari.helpers.AppSetupHelper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Creates a publication template for 3D views.
 */
public class V1_4__setup_3D_publishing extends BaseJavaMigration {

    private static final String TEMPLATE_JSON = "paikkis-3D-publish-template.json";
    private static final String METADATA_TEMPLATE_KEY = "publishTemplateUuid";
    private static final String APPLICATION_3D_NAME = "geoportal-3D";

    public void migrate(Context context) throws SQLException, IOException, JSONException {

        ViewService viewService =  new AppSetupServiceMybatisImpl();
        Connection connection = context.getConnection();

        // Create 3D publish template view
        long templateViewId = AppSetupHelper.create(connection, TEMPLATE_JSON);
        View templateView = viewService.getViewWithConf(templateViewId);

        // Set it as the publication template for the default 3D view.
        View geoportalView = viewService.getViewWithConfByUuId(AppSetupHelper.getUuidForDefaultSetup(connection, APPLICATION_3D_NAME));
        geoportalView.getMetadata().put(METADATA_TEMPLATE_KEY, templateView.getUuid());
        viewService.updateView(geoportalView);
    }
}