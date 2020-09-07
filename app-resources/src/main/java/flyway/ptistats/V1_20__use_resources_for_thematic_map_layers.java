package flyway.ptistats;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;

public class V1_20__use_resources_for_thematic_map_layers extends BaseJavaMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_20__use_resources_for_thematic_map_layers.class);

    private static final String RESOURCES_URL_PREFIX = "resources://";
    private static final String DIR = "stats-regionsets/";

    private enum ResourceStatLayer {

        NUTS1("dummy:nuts1", "nuts1_2017.json", "koodi_2", "nimi"),
        ERVA("dummy:erva", "erva_2017.json", "ERVAnro", "ERVA"),
        SHP("dummy:sairaanhoitopiiri", "sairaanhoitopiirit_2017.json", "Sairaanhoi", "nimi");

        final String layerName;
        final String resourcePath;
        final String regionIdTag;
        final String nameIdTag;

        private ResourceStatLayer(String layerName, String resourcePath, String regionIdTag, String nameIdTag) {
            this.layerName = layerName;
            this.resourcePath = resourcePath;
            this.regionIdTag = regionIdTag;
            this.nameIdTag = nameIdTag;
        }

        private JSONObject getAttributes() {
            JSONObject statistics = new JSONObject();
            JSONHelper.putValue(statistics, "featuresUrl", getFeaturesUrl());
            JSONHelper.putValue(statistics, "regionIdTag", regionIdTag);
            JSONHelper.putValue(statistics, "nameIdTag", nameIdTag);
            return JSONHelper.createJSONObject("statistics", statistics);
        }

        private String getFeaturesUrl() {
            return RESOURCES_URL_PREFIX + DIR + resourcePath;
        }

    }

    public void migrate(Context context) throws SQLException {
        String sql = "UPDATE oskari_maplayer "
                + "SET attributes = ?, url = ? "
                + "WHERE type = ? AND name = ?";
        try (PreparedStatement ps = context.getConnection().prepareStatement(sql)) {
            ps.setString(3, OskariLayer.TYPE_STATS);
            for (ResourceStatLayer layer : ResourceStatLayer.values()) {
                String attributes = layer.getAttributes().toString();
                ps.setString(1, attributes);
                ps.setString(2, layer.getFeaturesUrl());
                ps.setString(4, layer.layerName);
                LOG.debug("Executing:", ps.toString());
                int i = ps.executeUpdate();
                LOG.debug("Update result:", i);
            }
        }
    }

}
