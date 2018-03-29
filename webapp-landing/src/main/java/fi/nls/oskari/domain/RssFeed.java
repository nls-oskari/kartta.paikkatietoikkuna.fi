package fi.nls.oskari.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class RssFeed {

    private RssFeedChannel channel;

    public RssFeedChannel getChannel() {
        return channel;
    }

    public void setChannel(RssFeedChannel channel) {
        this.channel = channel;
    }
}
