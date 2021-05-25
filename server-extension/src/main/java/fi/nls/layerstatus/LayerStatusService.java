package fi.nls.layerstatus;

import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LayerStatusService extends OskariComponent {

    private Logger log = LogFactory.getLogger("STATUS");
    private Cache<JSONObject> cache;

    public void init() {
        cache = CacheManager.getCache(LayerStatusService.class.getName());
        cache.setLimit(10000);
        // should result in two weeks since the default is 30mins
        cache.setExpiration(cache.getExpiration() * 48 * 14);
    }

    public List<LayerStatus> getStatuses() {
        List<LayerStatus> list = cache.getKeys().stream()
                .map(layerId -> new LayerStatus(layerId, cache.get(layerId)))
                .collect(Collectors.toList());
        return list;
    }

    public List<JSONObject> getMostErrors(int limit) {
        List<JSONObject> mostErrors = getStatuses().stream()
                .sorted(Comparator.comparingLong(LayerStatus::getErrors).reversed())
                .limit(limit)
                .map(layer -> {
                    JSONObject o = new JSONObject();
                    JSONHelper.putValue(o, "id", layer.getId());
                    JSONHelper.putValue(o, "errors", layer.getErrors());
                    JSONHelper.putValue(o, "success", layer.getSuccess());
                    return o;
                })
                .collect(Collectors.toList());
        return mostErrors;
    }

    public List<JSONObject> getMostUsed(int limit) {
        List<JSONObject> mostSuccess = getStatuses().stream()
                .sorted(Comparator.comparingLong(LayerStatus::getRequestCount).reversed())
                .limit(limit)
                .map(layer -> {
                    JSONObject o = new JSONObject();
                    JSONHelper.putValue(o, "id", layer.getId());
                    JSONHelper.putValue(o, "errors", layer.getErrors());
                    JSONHelper.putValue(o, "success", layer.getSuccess());
                    return o;
                })
                .collect(Collectors.toList());
        return mostSuccess;
    }

    // {801: {errors: 0, success: 73, stack: [], previous: "success"}}
    public void saveStatus(JSONObject payload) {
        payload.keys().forEachRemaining(layerId -> {
            String id = (String) layerId;
            JSONObject layerData = payload.optJSONObject(id);
            layerData.remove("previous");
            // write log to get stacks for error debugging
            log.info(layerId, "-", layerData.toString());
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