package org.oskari.spatineo.serval.api;

import java.util.List;

public class ServalResponse {

    private String version;
    private String status;
    private String statusMessage;
    private List<ServalResult> result;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public List<ServalResult> getResult() {
        return result;
    }

    public void setResult(List<ServalResult> result) {
        this.result = result;
    }

}
