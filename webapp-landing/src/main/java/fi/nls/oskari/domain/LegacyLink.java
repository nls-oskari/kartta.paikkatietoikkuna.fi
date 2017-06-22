package fi.nls.oskari.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Comparator;

/**
 * Created by SMAKINEN on 20.6.2017.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class LegacyLink implements Comparable<LegacyLink> {
    public String path;
    public String newPath;
    public boolean passQuery;

    @Override
    public int compareTo(LegacyLink o) {
        return path.length() - o.path.length();
    }
}
