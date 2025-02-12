package fi.nls.oskari;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Support for link redirects. Configuration in linkredirect.properties
 *
 * @see fi.nls.oskari.spring.SpringConfig
 */
@Controller
public class FriendlyURLHandler {

    @Value("${ortophoto.url}")
    private String ortophotoUrl;

    @RequestMapping("/historiallisetilmakuvat")
    public ModelAndView finnish(HttpServletRequest request) throws Exception {
        return ortophotos("fi", request);
    }

    @RequestMapping("/sv/historiskaflygbilder")
    public ModelAndView swedish(HttpServletRequest request) throws Exception {
        return ortophotos("sv", request);
    }

    @RequestMapping("/en/historicalorthophotos")
    public ModelAndView english(HttpServletRequest request) throws Exception {
        return ortophotos("en", request);
    }

    public ModelAndView ortophotos(String lang, HttpServletRequest request) throws Exception {
        String url = attachQuery(ortophotoUrl, "lang=" + lang);
        return new ModelAndView("redirect:" + attachQuery(url, request.getQueryString()));
    }

    private String attachQuery(String path, String query) {
        if (query == null) {
            return path;
        }
        if (path.indexOf('?') == -1) {
            return path + "?" + query;
        }
        if (path.endsWith("?")) {
            return path + query;
        }
        return path + "&" + query;

    }
}
