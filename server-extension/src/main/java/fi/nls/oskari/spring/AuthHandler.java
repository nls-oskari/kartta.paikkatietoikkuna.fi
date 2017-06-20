package fi.nls.oskari.spring;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.extension.OskariParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Profile("preauth")
@Controller
@RequestMapping("/auth")
public class AuthHandler {

    private final static Logger LOG = LogFactory.getLogger(AuthHandler.class);

    @RequestMapping
    public ModelAndView index(@OskariParam ActionParameters params) throws Exception {
        LOG.info("User logged in:", params.getRequest().getHeader("auth-email"));
        return new ModelAndView("redirect:/");
    }

}