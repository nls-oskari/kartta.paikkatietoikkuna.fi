package flyway.paikkis;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppSetupHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marko Kuosmanen on 28.11.2017.
 */
public class V2_19__add_maplegend_to_ol3_view extends BaseJavaMigration {

    private static final String BUNDLE_ID = "maplegend";

    public void migrate(Context context) throws Exception {

        Connection connection = context.getConnection();
        final List<Long> views = getOl3Views(connection);
        for(Long viewId : views){
            if (AppSetupHelper.appContainsBundle(connection, viewId, BUNDLE_ID)) {
                continue;
            }
            AppSetupHelper.addBundleToApp(connection, viewId, BUNDLE_ID);
        }
    }

    private List<Long> getOl3Views(Connection conn) throws SQLException {
        List<Long> list = new ArrayList<>();
        String sql = "SELECT id FROM portti_view\n" +
                "WHERE name = 'Geoportal OL3'";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    list.add(rs.getLong("id"));
                }
            }
        }
        return list;
    }
}


