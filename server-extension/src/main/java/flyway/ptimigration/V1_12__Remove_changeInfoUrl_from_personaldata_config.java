package flyway.ptimigration;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

/**
 * Removes changeInfoUrl config from personaldata if available
 */
public class V1_12__Remove_changeInfoUrl_from_personaldata_config extends ConfigMigration {

    public String getBundle() {
        return "personaldata";
    }

    public String getModifiedConfig(final String param) throws Exception {
        final JSONObject config = JSONHelper.createJSONObject(param);
        config.remove("changeInfoUrl");
        return config.toString(2);
    }

}
