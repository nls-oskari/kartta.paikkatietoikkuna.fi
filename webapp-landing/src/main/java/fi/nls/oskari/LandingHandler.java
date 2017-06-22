package fi.nls.oskari;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by SMAKINEN on 2.6.2017.
 */
@Controller
@RequestMapping("/")
public class LandingHandler {


//    @Autowired
//    private MessageSource messages;

    @RequestMapping
    public String index() throws Exception {
        return "landingpage";
    }
}