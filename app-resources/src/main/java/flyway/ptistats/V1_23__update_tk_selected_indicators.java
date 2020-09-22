package flyway.ptistats;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.SQLException;

/**
 * As Tilastokeskus PXWeb config is changed to parse a PX-table variables into indicators -> any saved indicator refs
 * need to be migrated to match the config.
 */
public class V1_23__update_tk_selected_indicators extends BaseJavaMigration {

    public void migrate(Context context) throws SQLException {
        // already migrated, no need to run on 3.0+
    }

}
