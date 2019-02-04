package fi.nls.oskari;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.domain.RssFeedChannel;
import fi.nls.oskari.domain.RssFeedItem;
import fi.nls.oskari.domain.RssFeedXmlRoot;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RssFeedService {

    private static final Logger logger = LogManager.getLogger("RssFeedReader");

    @Value("${rss.url.notifications}")
    private String notificationsUrl;

    @Value("${rss.url.news}")
    private String newsUrl;

    @Value("${rss.maxEntries}")
    private int maxEntries;

    private RssFeedChannel notificationsChannel;
    private RssFeedChannel newsChannel;
    private List<RssFeedItem> notifications;
    private List<RssFeedItem> news;
    private HttpComponentsClientHttpRequestFactory clientHttpRequestFactory;
    private ObjectMapper mapper;

    public RssFeedService() {
        clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        mapper = new ObjectMapper();
    }

    public List<RssFeedItem> getNotifications() {
        return notifications;
    }

    public List<RssFeedItem> getNews() {
        return news;
    }

    private RssFeedChannel readRssFeed(String feedUrl) {
        try {
            RssFeedChannel channel = null;
            ClientHttpRequest req = clientHttpRequestFactory.createRequest(new URI(feedUrl), HttpMethod.GET);
            ClientHttpResponse resp = req.execute();

            if (resp.getStatusCode() == HttpStatus.OK) {
                try {
                    String xml = StreamUtils.copyToString(resp.getBody(), Charset.forName("UTF-8"));
                    JSONObject jsonObject = XML.toJSONObject(xml);

                    RssFeedXmlRoot rssFeedXmlRoot = mapper.readValue(jsonObject.toString(), RssFeedXmlRoot.class);
                    channel = rssFeedXmlRoot.getRssFeed().getChannel();
                }
                catch (Exception ex) {
                    logger.error("Couldn't deserialize RssFeedChannel " +feedUrl, ex);
                }
                return channel;
            }
            else {
                logger.warn("RSS feed reader got status " + resp.getRawStatusCode() + "for url " + feedUrl);
            }
        }
        catch (Exception ex) {
            logger.error("Reading RSS feed failed", ex);
        }
        return null;
    }

    /**
     * Picks last X items from the feed.
     *
     * @param channel
     * @return
     */
    private List<RssFeedItem> pickLatestItems(RssFeedChannel channel) {
        if (channel != null && channel.getItems() != null) {
            List<RssFeedItem> items = channel.getItems();
            Collections.sort(items);
            return items.subList(0, items.size() < maxEntries ? items.size() : maxEntries);
        }
        return new ArrayList<>();
    }

    @Scheduled(fixedDelayString = "${rss.readIntervalMs}", initialDelayString = "${rss.initialReadDelayMs}")
    private void readRssFeeds() {
        try {
            RssFeedChannel updatedNotifications = readRssFeed(notificationsUrl);
            if (updatedNotifications != null) {
                notificationsChannel = updatedNotifications;
                notifications = pickLatestItems(notificationsChannel);
            }
            RssFeedChannel updatedNews = readRssFeed(newsUrl);
            if (updatedNews != null) {
                newsChannel = updatedNews;
                news = pickLatestItems(newsChannel);
            }
        }
        catch (Exception ex) {
            logger.error("Reading RSS feeds caused an exception", ex);
        }
    }
}
