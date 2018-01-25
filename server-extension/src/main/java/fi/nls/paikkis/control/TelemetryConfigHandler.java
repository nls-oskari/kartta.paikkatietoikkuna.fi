package fi.nls.paikkis.control;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.control.view.modifier.bundle.BundleHandler;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

@OskariViewModifier("telemetry")
public class TelemetryConfigHandler extends BundleHandler {

    private static final String ENDPOINT_URL = "endpoint";

    @Override
    public boolean modifyBundle(ModifierParams params) throws ModifierException {
        final JSONObject config = getBundleConfig(params.getConfig());
        if(!config.has(ENDPOINT_URL)) {
            // only write if not configured in db
            String value = PropertyUtil.getOptional("paikkis.telemetry.endpoint");
            if(value != null) {
                JSONHelper.putValue(config, ENDPOINT_URL, value);
                return true;
            }
        }
        return false;
    }
}
