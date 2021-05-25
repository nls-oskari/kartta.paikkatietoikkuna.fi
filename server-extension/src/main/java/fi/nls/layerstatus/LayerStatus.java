package fi.nls.layerstatus;

import org.json.JSONObject;

public class LayerStatus {

    private final String id;
    private long errors = 0;
    private long success = 0;

    LayerStatus(String id, JSONObject data) {
        this.id = id;
        this.errors = data.optLong("errors");
        this.success = data.optLong("success");
    }
    
    public String getId() {
        return id;
    }

    public long getErrors() {
        return errors;
    }

    public long getSuccess() {
        return success;
    }
    public long getRequestCount() {
        return success + errors;
    }
}
