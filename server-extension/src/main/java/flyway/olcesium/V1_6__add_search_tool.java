package flyway.olcesium;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.AppSetupServiceMybatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.util.FlywayHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class V1_6__add_search_tool implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_6__add_search_tool.class);
    private static final String CESIUM_VIEW_NAME = "Geoportal Ol Cesium";
    private static final String SEARCH_BUNDLE_NAME = "search";
    private static final String LAYER_SELECTOR = "layerselector2";

    private ViewService viewService = null;

    public void migrate(Connection connection) {
        viewService =  new AppSetupServiceMybatisImpl();
        updateCesiumViews(connection);
    }

    private void updateCesiumViews(Connection connection) {
        try {
            List<Long> viewIds = getCesiumViewIds(connection);

            for(Long viewId : viewIds) {
                int searchBundleSeqNo = -1;

                // Position search bundle before layer selector
                View modifyView = viewService.getViewWithConf(viewId);

                List<Bundle> filteredBundles = modifyView.getBundles()
                        .stream()
                        .filter((bundle) -> bundle.getBundleinstance().equals(LAYER_SELECTOR))
                        .limit(1)
                        .collect(Collectors.toList());

                if (!filteredBundles.isEmpty()) {
                    final int seqNo = filteredBundles.get(0).getSeqNo();
                    // increase bundle seqno by one
                    modifyView.getBundles()
                            .stream()
                            // update in reversed seqno order to prevent constraint errors
                            .sorted(Comparator.comparingInt(Bundle::getSeqNo).reversed())
                            .forEach((bundle -> {
                                if (bundle.getSeqNo() >= seqNo) {
                                    bundle.setSeqNo(bundle.getSeqNo() + 1);
                                    try {
                                        FlywayHelper.updateBundleInView(connection, bundle, viewId);
                                    } catch (SQLException ex) {
                                        LOG.error(ex, "Error updating bundle startup order for view " + viewId);
                                    }
                                }
                            }));

                    searchBundleSeqNo = seqNo;
                }
                addSearchBundle(connection, viewId, searchBundleSeqNo);
            }
        } catch (Exception e) {
            LOG.error(e, "Error setting search bundle for Cesium views");
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

    private static void addSearchBundle(Connection connection, Long viewId, int seqNo)
            throws SQLException {
        String sql ="INSERT INTO portti_view_bundle_seq" +
                "(view_id, bundle_id, seqno, config, state, startup, bundleinstance) " +
                "VALUES (" +
                "?, " +
                "(SELECT id FROM portti_bundle WHERE name=?), " +
                (seqNo != -1 ? "?" : "(SELECT max(seqno) + 1 FROM portti_view_bundle_seq WHERE view_id=?)") + ", " +
                "(SELECT config FROM portti_bundle WHERE name=?), " +
                "(SELECT state FROM portti_bundle WHERE name=?),  " +
                "(SELECT startup FROM portti_bundle WHERE name=?), " +
                "?)";
        try(final PreparedStatement statement =
                    connection.prepareStatement(sql)) {
            int i = 0;
            statement.setLong(++i, viewId);
            statement.setString(++i, SEARCH_BUNDLE_NAME);
            if (seqNo != -1) {
                statement.setInt(++i, seqNo);
            } else {
                statement.setLong(++i, viewId);
            }
            statement.setString(++i, SEARCH_BUNDLE_NAME);
            statement.setString(++i, SEARCH_BUNDLE_NAME);
            statement.setString(++i, SEARCH_BUNDLE_NAME);
            statement.setString(++i, SEARCH_BUNDLE_NAME);
            statement.execute();
        }
    }
}
