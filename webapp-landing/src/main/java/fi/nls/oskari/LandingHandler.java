package fi.nls.oskari;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by SMAKINEN on 2.6.2017.
 */
@Controller
@RequestMapping("/")
public class LandingHandler {


    @RequestMapping
    public String index() throws Exception {
        return "landingpage";
    }
}