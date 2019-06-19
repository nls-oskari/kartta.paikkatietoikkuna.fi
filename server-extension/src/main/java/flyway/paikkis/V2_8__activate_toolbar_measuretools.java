package flyway.paikkis;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;


/**
 * Removes measuretools configs from appsetups that are not "published" or publish template.
 * This restores the basic measurement functionality to all appsetups
 */
public class V2_8__activate_toolbar_measuretools extends ConfigMigration {

    public String getBundle() {
        return "toolbar";
    }

    /**
     * Only edit appsetups that are NOT the publish template OR a published map
     * @return
     */
    public String getAdditionalSQLFilterForBundles() {
        return "AND view_id IN (select id from portti_view where type NOT IN ('PUBLISH', 'PUBLISHED'))";
    }

    /**
     * Configs to be removed:
     *  - basictools : {measureline: false, measurearea: false}
     *  - viewtools :  {print: false}
     * @param param
     * @return
     * @throws Exception
     */
    public String getModifiedConfig(final String param) throws Exception {
        final JSONObject config = JSONHelper.createJSONObject(param);
        if(config == null) {
            return null;
        }
        final JSONObject basictools = config.optJSONObject("basictools");
        if(basictools != null) {
            // remove measureline/area configs as we want them activated
            basictools.remove("measureline");
            basictools.remove("measurearea");
            if(basictools.length() == 0) {
                // remove basictools config if it's empty after this
                config.remove("basictools");
            }
        }
        // print isn't handled anymore by the frontend so remove it if we still have it
        final JSONObject viewtools = config.optJSONObject("viewtools");
        if(viewtools != null) {
            viewtools.remove("print");
            if(viewtools.length() == 0) {
                // remove viewtools config if it's empty after this
                config.remove("viewtools");
            }
        }
        return config.toString(2);
    }
}