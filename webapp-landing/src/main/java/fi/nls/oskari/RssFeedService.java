package fi.nls.oskari;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.domain.RssFeedChannel;
import fi.nls.oskari.domain.RssFeedItem;
import fi.nls.oskari.domain.RssFeedXmlRoot;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

@Service
public class RssFeedService implements Runnable {

    private static final String FEED_URL_NOTIFICATIONS = "http://www.maanmittauslaitos.fi/rss/palvelutiedotteet/184";
    private static final String FEED_URL_NEWS = "http://www.maanmittauslaitos.fi/rss/paikkatietojen-yhteiskaytto";
    private static int READ_INTERVAL = 60000  * 30; //30 minutes
    private static int NUM_SHOW_ITEMS = 5;

    private static final Logger logger = Logger.getLogger("RssFeedReader");

    private RssFeedChannel notificationsChannel;
    private RssFeedChannel newsChannel;
    private List<RssFeedItem> notifications;
    private List<RssFeedItem> news;
    private HttpComponentsClientHttpRequestFactory clientHttpRequestFactory;
    private ObjectMapper mapper;

    public RssFeedService() {
        clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("ddd, dd MMM yyyy HH:mm:SS Z"));
        new Thread(this).start();
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
                    ObjectMapper objectMapper = new ObjectMapper();

                    RssFeedXmlRoot rssFeedXmlRoot = objectMapper.readValue(jsonObject.toString(), RssFeedXmlRoot.class);
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

    private void updateRssItemLists() {
        notifications = pickLatestItems(notificationsChannel);
        news = pickLatestItems(newsChannel);
    }

    /**
     * Picks last X items from the feed. Presumes that items are in chronological order.
     *
     * @param channel
     * @return
     */
    private List<RssFeedItem> pickLatestItems(RssFeedChannel channel) {
        List<RssFeedItem> list = new Vector<RssFeedItem>();
        if (channel != null && channel.getItems() != null) {
            Collections.sort(channel.getItems());
            for (int i = 0; i < channel.getItems().size() && i < NUM_SHOW_ITEMS; i++) {
                list.add(channel.getItems().get(i));
            }
        }
        return list;
    }

    @Override
    public void run() {
        try{
            while(!Thread.currentThread().isInterrupted()) {
                notificationsChannel = readRssFeed(FEED_URL_NOTIFICATIONS);
                newsChannel = readRssFeed(FEED_URL_NEWS);
                updateRssItemLists();
                Thread.sleep(READ_INTERVAL);
            }
        }
        catch(InterruptedException ex){
            logger.error("RssFeedReader interrupted, it won't read feeds anymore!", ex);
        }
    }
}
