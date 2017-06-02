package fi.nls.oskari.spring;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.extension.OskariParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by SMAKINEN on 2.6.2017.
 */
@Controller
@RequestMapping("/auth")
public class AuthHandler {

    private final static Logger LOG = LogFactory.getLogger(AuthHandler.class);

    @RequestMapping
    public @ResponseBody
    Map<String,String> index(@OskariParam ActionParameters params) throws Exception {
        Enumeration<String> names = params.getRequest().getHeaderNames();
        Map<String, String> headers = new HashMap<>();
        while(names.hasMoreElements()) {
            final String key = names.nextElement().toLowerCase();
            if(key.startsWith("auth") || "true".equalsIgnoreCase(params.getHttpParam("all"))) {
                headers.put(key, params.getHttpHeader(key));
            }
        }
        return headers;
    }
}