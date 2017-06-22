package fi.nls.oskari;

import fi.nls.oskari.domain.LegacyLink;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fi.nls.oskari.Helper.getBasePath;
import static fi.nls.oskari.Helper.getMapper;

/**
 * Support for old document links, now available under Jetty/resources/legacy-docs
 */
@Controller
public class LegacyMapLinksHandler {

    private List<LegacyLink> links = new ArrayList<>();

    @PostConstruct
    private void readList() throws IOException {
        LegacyLink[] list = getMapper().readValue(getClass().getResourceAsStream(getBasePath() + "/liferay_links.json"), LegacyLink[].class);
        links.addAll(Arrays.asList(list));
        Collections.sort(links);
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
            System.out.println(link.path);
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