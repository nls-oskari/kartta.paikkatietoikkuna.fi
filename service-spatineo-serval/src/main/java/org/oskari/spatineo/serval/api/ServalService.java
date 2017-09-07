package org.oskari.spatineo.serval.api;

public class ServalService {

    public enum ServalServiceType {
        WFS, WMS 
    }

    private final String type;
    private final String url;
    private final String offering;

    public ServalService(ServalServiceType type, String url, String offering) {
        this.type = type.toString();
        this.url = url;
        this.offering = offering;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getOffering() {
        return offering;
    }

}
