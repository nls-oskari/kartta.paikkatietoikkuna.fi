package flyway.paikkis;

import fi.nls.oskari.util.FlywayHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marko Kuosmanen on 28.11.2017.
 */
public class V2_19__add_maplegend_to_ol3_view implements JdbcMigration {

    private static final String BUNDLE_ID = "maplegend";

    public void migrate(Connection connection) throws Exception {

        final List<Long> views = getOl3Views(connection);
        for(Long viewId : views){
            if (FlywayHelper.viewContainsBundle(connection, BUNDLE_ID, viewId)) {
                continue;
            }
            FlywayHelper.addBundleWithDefaults(connection, viewId, BUNDLE_ID);
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


