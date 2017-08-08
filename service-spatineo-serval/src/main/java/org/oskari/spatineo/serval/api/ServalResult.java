package org.oskari.spatineo.serval.api;

public class ServalResult {

    private String status;
    private String statusMessage;
    private ServalStats week;
    private ServalStats year;
    private String infoUrl;

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

    public ServalStats getWeek() {
        return week;
    }

    public void setWeek(ServalStats week) {
        this.week = week;
    }

    public ServalStats getYear() {
        return year;
    }

    public void setYear(ServalStats year) {
        this.year = year;
    }

    public String getInfoUrl() {
        return infoUrl;
    }

    public void setInfoUrl(String infoUrl) {
        this.infoUrl = infoUrl;
    } 

}
