package flyway.pti3d;

import fi.nls.oskari.db.ViewHelper;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

/**
 * Creates a publication template for 3D views.
 */
public class V1_4__setup_3D_publishing implements JdbcMigration {

    private static final String TEMPLATE_JSON = "paikkis-3D-publish-template.json";
    private static final String METADATA_TEMPLATE_KEY = "publishTemplateUuid";

    private ViewService viewService;

    public void migrate(Connection connection) throws Exception {

        viewService =  new AppSetupServiceMybatisImpl();

        // Create 3D publish template view
        long templateViewId = ViewHelper.insertView(connection, TEMPLATE_JSON);
        View templateView = viewService.getViewWithConf(templateViewId);

        // Set it as the publication template for the default 3D view.
        View geoportalView = viewService.getViewWithConfByUuId(FlywayHelper3D.get3DViewUuid(connection));
        geoportalView.getMetadata().put(METADATA_TEMPLATE_KEY, templateView.getUuid());
        viewService.updateView(geoportalView);
    }
}