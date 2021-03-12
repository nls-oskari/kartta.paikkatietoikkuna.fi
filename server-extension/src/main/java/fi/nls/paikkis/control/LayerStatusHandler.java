package fi.nls.paikkis.control;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@OskariActionRoute("LayerStatus")
public class LayerStatusHandler extends RestActionHandler {

    private Logger log = LogFactory.getLogger("STATUS");
    private Cache<JSONObject> cache;

    private class Layer {
        String id;
        long errors = 0;
        long success = 0;

        Layer(String id, JSONObject data) {
            this.id = id;
            this.errors = data.optLong("errors");
            this.success = data.optLong("success");
        }

        public long getErrors() {
            return errors;
        }

        public long getSuccess() {
            return success;
        }
    }

    public void init() {
        cache = CacheManager.getCache(LayerStatusHandler.class.getName());
        cache.setLimit(10000);
        // should result in two weeks since the default is 30mins
        cache.setExpiration(cache.getExpiration() * 48 * 14);
    }

    public void handleGet(ActionParameters params) throws ActionDeniedException {
        params.requireAdminUser();
        int limit = params.getHttpParam("limit", 20);
        final JSONObject response = new JSONObject();

        List<Layer> list = cache.getKeys().stream()
            .map(layerId -> new Layer(layerId, cache.get(layerId)))
            .collect(Collectors.toList());

        JSONHelper.putValue(response, "layerCount", list.size());

        List<JSONObject> mostErrors = list.stream()
                .sorted(Comparator.comparingLong(Layer::getErrors).reversed())
                .limit(limit)
                .map(layer -> {
                    JSONObject o = new JSONObject();
                    JSONHelper.putValue(o, "id", layer.id);
                    JSONHelper.putValue(o, "errors", layer.errors);
                    JSONHelper.putValue(o, "success", layer.success);
                    return o;
                })
                .collect(Collectors.toList());
        JSONHelper.putValue(response, "errorsTop", new JSONArray(mostErrors));

        List<JSONObject> mostSuccess = list.stream()
                .sorted(Comparator.comparingLong(Layer::getSuccess).reversed())
                .limit(limit)
                .map(layer -> {
                    JSONObject o = new JSONObject();
                    JSONHelper.putValue(o, "id", layer.id);
                    JSONHelper.putValue(o, "errors", layer.errors);
                    JSONHelper.putValue(o, "success", layer.success);
                    return o;
                })
                .collect(Collectors.toList());
        JSONHelper.putValue(response, "successTop", new JSONArray(mostSuccess));

        ResponseHelper.writeResponse(params, response);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionParamsException {
        // {801: {errors: 0, success: 73, stack: [], previous: "success"}}
        JSONObject payload = params.getPayLoadJSON();
        payload.keys().forEachRemaining(layerId -> {
            String id = (String) layerId;
            JSONObject layerData = payload.optJSONObject(id);
            // write log to get stacks for error debugging
            log.info(layerData.toString());
            // write to cache so we can examine combined error counts for all nodes
            JSONObject value = cache.get(id);
            if (value != null) {
                long successCount = value.optLong("success", 0) + layerData.optLong("success", 0);
                JSONHelper.putValue(layerData, "success", successCount);
                long errorCount = value.optLong("errors", 0) + layerData.optLong("errors", 0);
                JSONHelper.putValue(layerData, "errors", errorCount);
            }
            cache.put(id, layerData);
        });
    }
}