package flyway.ptistats;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

/**
 * Updates references to indicator ids after 1.44 migration since id also references px-file name that changes yearly
 */
public class V1_59__update_refs_to_saved_indicators_tk extends V1_47__update_refs_to_saved_indicators_tk {
    private static final Logger LOG = LogFactory.getLogger(V1_59__update_refs_to_saved_indicators_tk.class);
    private static final String ID_SEPARATOR = "::";

    // for overriding purposes next year...
    // though next year we'll need a new version of getBundleStates() as it's
    // SELECT appsetup_id, seqno, state FROM oskari_appsetup_bundles WHERE bundle_id = ? AND state LIKE '%kuntien_avainluvut%'
    public String getNewFileName() {
        return "142h.px";
    }
    public String getOldFileName() {
        return "kuntien_avainluvut_2021_aikasarja.px";
    }

    protected boolean updateState(JSONObject state) {
        boolean modified = super.updateState(state);
        if (modified) {
            String activeValue = state.optString("active", "");
            if (activeValue.contains(getOldFileName())) {
                // FROM: 3_kuntien_avainluvut_2021_aikasarja.px::M411_Vuosi="2021"
                // TO:   3_142h.px::M411_Vuosi="2021"
                String newActiveValue = activeValue.replace(getOldFileName(), getNewFileName());
                JSONHelper.putValue(state, "active", newActiveValue);
            }
        }
        return modified;
    }

    protected String getNewId(String oldId) {
        String[] parts = oldId.split(ID_SEPARATOR);
        if (parts.length != 2 || !parts[0].equalsIgnoreCase(getOldFileName())) {
            // something weird here
            LOG.warn("Unidentified id:", oldId);
            return oldId;
        }
        return getNewFileName() + ID_SEPARATOR + parts[1];
    }
}
