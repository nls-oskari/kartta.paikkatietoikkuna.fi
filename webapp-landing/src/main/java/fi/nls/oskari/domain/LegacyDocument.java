package fi.nls.oskari.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by SMAKINEN on 20.6.2017.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class LegacyDocument {
    @JsonProperty("UUID")
    public String uuid;
    @JsonProperty("title")
    public String filename;
    public String mimeType;
    @JsonProperty("filename")
    public String path;
}
