package fi.nls.oskari;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by SMAKINEN on 2.6.2017.
 */
@Controller
@RequestMapping("/")
public class LandingHandler {
    private static final Logger logger404 = LogManager.getLogger("BrokenURL");

    @Autowired
    private RssFeedService rssService;

    @RequestMapping
    public String index(HttpServletRequest request) throws Exception {
        request.setAttribute("notifications", rssService.getNotifications());
        request.setAttribute("news", rssService.getNews());
        return "landingpage";
    }

    @RequestMapping("**")
    public ModelAndView handleAnyRequest(HttpServletRequest request) {
        StringBuilder b = new StringBuilder(request.getRequestURI());
        final String query = request.getQueryString();
        if(query != null) {
            b.append("?");
            b.append(query);
        }
        logger404.info(b.toString() + " referer: " + request.getHeader("referer"));
        return new ModelAndView("redirect:/");
    }
}