package fi.nls.oskari.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown=true)
public class RssFeedItem implements Comparable<RssFeedItem> {

    private String title;
    private String link;
    private Date pubDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    @Override
    public int compareTo(RssFeedItem o) {
        if (pubDate == null && o.pubDate == null) {
            return 0;
        }
        if (pubDate == null) {
            return 1;
        }
        if (o.pubDate == null) {
            return -1;
        }
        // Descending order latest first
        return pubDate.compareTo(o.pubDate) * -1;
    }
}
