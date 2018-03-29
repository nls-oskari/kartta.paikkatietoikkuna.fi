package fi.nls.oskari.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class RssFeedChannel extends RssFeedItem {

    private String language;
    @JsonProperty("item")
    private List<RssFeedItem> items;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<RssFeedItem> getItems() {
        return items;
    }

    public void setItems(List<RssFeedItem> items) {
        this.items = items;
    }
}
