package fi.nls.oskari.spring;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by SMAKINEN on 20.6.2017.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class LegacyDocument {
    @JsonProperty("UUID")
    String uuid;
    @JsonProperty("title")
    String filename;
    String mimeType;
    @JsonProperty("filename")
    String path;
}
