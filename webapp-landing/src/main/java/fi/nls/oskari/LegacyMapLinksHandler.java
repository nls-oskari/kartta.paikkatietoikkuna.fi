package fi.nls.oskari;

import fi.nls.oskari.domain.LegacyLink;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

import static fi.nls.oskari.Helper.getBasePath;
import static fi.nls.oskari.Helper.getMapper;

/**
 * Support for old document links, now available under Jetty/resources/legacy-docs
 */
@Controller
public class LegacyMapLinksHandler {

    private static final Logger LOG = LogManager.getLogger("PublishedMaps");

    private List<LegacyLink> links = new ArrayList<>();
    private Map<String, String> liferay5MapLinks = new HashMap<>();

    @PostConstruct
    private void readList() throws IOException {
        LegacyLink[] list = getMapper().readValue(getClass().getResourceAsStream(getBasePath() + "/liferay_links.json"), LegacyLink[].class);
        links.addAll(Arrays.asList(list));
        Collections.sort(links);

        liferay5MapLinks = getMapper().readValue(getClass().getResourceAsStream(getBasePath() + "/liferay_oldId_uuid_maps.json"), HashMap.class);
    }

    @RequestMapping("/published/{mapId}")
    public ModelAndView simpleEmbeddedMaps(@PathVariable("mapId") String mapId,
                                     @RequestParam(value = "lang", required = false, defaultValue = "fi") String lang,
                                     HttpServletRequest request) throws Exception {
        return embeddedMaps(lang, mapId, request);
    }

    @RequestMapping("/published/{lang}/{mapId}")
    public ModelAndView embeddedMaps(@PathVariable("lang") String lang, @PathVariable("mapId") String mapId, HttpServletRequest request) throws Exception {
        LOG.info(mapId + " referer: " + request.getHeader("referer"));
        String url = "https://kartta.paikkatietoikkuna.fi/?lang=" + lang + "&";
        if (isInteger(mapId)) {
            // digits -> use viewId
            url = url + "viewId=" + mapId;
        } else {
            // default to uuid
            url = url + "uuid=" + mapId;
        }
        return new ModelAndView("redirect:" + attachQuery(url, request.getQueryString()));
    }

    private static boolean isInteger(String str) {
        try {
            Long.parseUnsignedLong(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // /widget/web/fi/julkaisijankartta/-/MapPublished_WAR_mapportlet?id=442
    @RequestMapping("/widget/web/fi/julkaisijankartta/-/MapPublished_WAR_mapportlet")
    public ModelAndView liferay5embeddedMaps(@RequestParam("id") String mapId,
                                      @RequestParam(value = "lang", required = false, defaultValue = "fi") String lang,
                                      HttpServletRequest request) throws Exception {
        String liferayMapId = liferay5MapLinks.get(mapId);
        if (liferayMapId == null) {
            LOG.info(mapId + " referer: " + request.getHeader("referer"));
            // not found -> go to landing page
            return new ModelAndView("redirect:/");
        }
        return embeddedMaps(lang, liferayMapId, request);
    }

    @RequestMapping("/web/**")
    public ModelAndView simpleMapping(HttpServletRequest request) throws Exception {
        String redirect = getBestMatch(request.getRequestURI(), request.getQueryString());
        if(redirect != null) {
            return new ModelAndView("redirect:" + redirect);
        }
        // not found -> go to landing page
        return new ModelAndView("redirect:/");
    }

    private String getBestMatch(String path, String query) {
        // links are sorted from longest to shortest so the first one to match is the best one
        String candidate = null;
        for(LegacyLink link : links) {
            if(!link.path.startsWith(path)) {
                continue;
            }
            // use possible shorter match as candidate so /web/fi has a candidate to be /web/fi/kartta,
            // but is not returned automatically if there's a mapping for /web/fi as well
            candidate = attachQuery(link.newPath, query);
            if(path.length() >= link.path.length()) {
                // return longer or exact match straight away
                return candidate;
            }
        }
        return candidate;
    }

    private String attachQuery(String path, String query) {
        if(query == null) {
            return path;
        }
        if(path.indexOf('?') == -1) {
            return path + "?" + query;
        }
        if(path.endsWith("?")) {
            return path + query;
        }
        return path + "&" + query;

    }

}