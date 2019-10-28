package flyway.pti3d;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FlywayHelper3D {

    private static final Logger LOG = LogFactory.getLogger(FlywayHelper3D.class);
    private static final String DEFAULT_VIEW_TYPE = "DEFAULT";

    public static String get3DViewUuid(Connection conn, String applicationName) {
        final String sql = "SELECT uuid FROM portti_view WHERE application=? and type=? limit 1";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, applicationName);
            statement.setString(2, DEFAULT_VIEW_TYPE);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    return rs.getString("uuid");
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Error getting 3D portti view uuid");
        }
        return null;
    }
}
