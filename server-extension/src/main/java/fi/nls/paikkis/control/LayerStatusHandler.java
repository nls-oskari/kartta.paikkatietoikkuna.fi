package fi.nls.paikkis.control;

import fi.nls.layerstatus.LayerStatusService;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

@OskariActionRoute("LayerStatus")
public class LayerStatusHandler extends RestActionHandler {

    private LayerStatusService getService() {
        return OskariComponentManager.getComponentOfType(LayerStatusService.class);
    }

    public void handleGet(ActionParameters params) throws ActionDeniedException {
        params.requireAdminUser();

        int limit = params.getHttpParam("limit", 20);
        LayerStatusService service = getService();

        final JSONObject response = new JSONObject();
        JSONHelper.putValue(response, "layerCount", service.getStatuses().size());
        JSONHelper.putValue(response, "errorsTop", new JSONArray(service.getMostErrors(limit)));
        JSONHelper.putValue(response, "mostUsed", new JSONArray(service.getMostUsed(limit)));

        ResponseHelper.writeResponse(params, response);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionParamsException {
        JSONObject payload = params.getPayLoadJSON();
        LayerStatusService service = getService();
        service.saveStatus(payload);
    }
}