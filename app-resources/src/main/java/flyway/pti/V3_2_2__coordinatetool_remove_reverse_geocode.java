package flyway.pti;

import fi.nls.oskari.domain.map.view.Bundle;
import org.json.JSONObject;

/**
 * Remove reverse geocoding channel from coordinatetool for all appsetups.
 *
 * The feature isn't used in PTI currently and there are some configs on the database that can be cleaned.
 */
public class V3_2_2__coordinatetool_remove_reverse_geocode extends V3_2_0__coordinatetool_remove_reverse_geocode {
    protected boolean updateBundle(Bundle bundle) {
        if (bundle == null) {
            return false;
        }
        // This will be the migration to run once testing is complete:
        JSONObject conf = bundle.getConfigJSON();
        boolean updateRequired = conf.remove("isReverseGeocode") != null;
        updateRequired = updateRequired || conf.remove("reverseGeocodingIds") != null;
        bundle.setConfig(conf.toString());
        return updateRequired;
    }
}
