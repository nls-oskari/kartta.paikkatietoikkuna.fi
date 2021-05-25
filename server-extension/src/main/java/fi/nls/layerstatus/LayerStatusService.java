package fi.nls.layerstatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Oskari
public class LayerStatusService extends OskariComponent {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String REDIS_KEY = "LayerStatus";
    private Logger log = LogFactory.getLogger("STATUS");

    public List<LayerStatus> getStatuses() {
        return listFromRedis();
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
            updateToRedis(
                    id,
                    layerData.optLong("success", 0),
                    layerData.optLong("errors", 0)
            );
            // write log to get stacks for error debugging
            log.info(layerId, "-", layerData.toString());
        });
    }

    private List<LayerStatus> listFromRedis() {
        Set<String> keys = JedisManager.hkeys(REDIS_KEY);
        return keys.stream()
                .map(layerId -> getEntry(layerId))
                .collect(Collectors.toList());
    }

    private void updateToRedis(String id, long success, long errors) {
        // TODO: should use https://redis.io/commands/hincrby instead or save to postgres?
        LayerStatus status = getEntry(id);
        status.addToErrors(errors);
        status.addToSuccess(success);
        JedisManager.hset(REDIS_KEY, id, writeAsJSON(status));
        // TODO: bake id into key and use date string as field (current id) to get time dimension?
        // Set<String> keys = JedisManager.hkeys(REDIS_KEY)
    }

    private LayerStatus getEntry(String id) {
        String data = JedisManager.hget(REDIS_KEY, id);
        if (data == null) {
            return new LayerStatus(id);
        }
        return readFromJSON(data);
    }

    private LayerStatus readFromJSON(String status) {
        try {
            return MAPPER.readValue(status, LayerStatus.class);
        } catch (JsonProcessingException e) {
            throw new ServiceRuntimeException("Unable to deserialize status", e);
        }
    }

    private String writeAsJSON(LayerStatus status) {
        try {
            return MAPPER.writeValueAsString(status);
        } catch (JsonProcessingException e) {
            throw new ServiceRuntimeException("Unable to serialize status", e);
        }
    }
}