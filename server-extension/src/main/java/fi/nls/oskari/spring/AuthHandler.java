package fi.nls.oskari.spring;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.extension.OskariParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthHandler {

    private final static Logger LOG = LogFactory.getLogger(AuthHandler.class);

    /*
        Headers:
        "auth-email":"asdf@asdf.fi",
        "auth-firstname":"asdf",
        "auth-lastname":"0xNNNNNNNN",
        "auth-screenname":"asdf",
        "auth-nlsadvertisement":""
    */
    @RequestMapping
    public @ResponseBody
    Map<String,String> index(@OskariParam ActionParameters params) throws Exception {
        LOG.info("User logged in:", params.getRequest().getHeader("auth-email"));
        Enumeration<String> names = params.getRequest().getHeaderNames();
        Map<String, String> headers = new HashMap<>();
        while(names.hasMoreElements()) {
            final String key = names.nextElement().toLowerCase();
            if(key.startsWith("auth") || "true".equalsIgnoreCase(params.getHttpParam("all"))) {
                headers.put(key, getValue(params.getHttpHeader(key)));
            }
        }
        return headers;
    }

    /**
     * Some values can be encoded
     * @param input
     * @return
     */
    public String getValue(String input) {
        if(input == null) {
            return null;
        }
        if(!input.startsWith("0x")) {
            return input;
        }
        input = input.substring(2);
        byte[] bytes = DatatypeConverter.parseHexBinary(input);
        return new String(bytes, Charset.forName("UTF-8"));
    }
}