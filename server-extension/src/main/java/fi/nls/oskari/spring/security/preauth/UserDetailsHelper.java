package fi.nls.oskari.spring.security.preauth;

import fi.nls.oskari.domain.User;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.util.Enumeration;

/**
 * Created by SMAKINEN on 19.6.2017.
 */
public class UserDetailsHelper {

    private enum HEADER {
        email,
        firstname,
        lastname,
        screenname;

        public static boolean contains(String test) {

            for (HEADER h : values()) {
                if (h.name().equals(test)) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Parses request headers for user info like:
     * "auth-email":"asdf@asdf.fi"
     * "auth-firstname":"asdf"
     * "auth-lastname":"0xNNNNNNNN"
     * "auth-screenname":"asdf"
     * "auth-nlsadvertisement":""
     */
    public static User parseUserFromHeaders(HttpServletRequest request, String headerPrefix) {

        User user = new User();
        user.setEmail(getHeader(request, headerPrefix + HEADER.email));
        user.setFirstname(getHeader(request, headerPrefix + HEADER.firstname));
        user.setLastname(getHeader(request, headerPrefix + HEADER.lastname));
        user.setScreenname(getHeader(request, headerPrefix + HEADER.screenname));

        // add all other headers with prefix to user attributes
        Enumeration<String> names = request.getHeaderNames();
        int prefixLen = headerPrefix.length();
        while(names.hasMoreElements()) {
            final String key = names.nextElement().toLowerCase();
            if(!key.startsWith(headerPrefix)) {
                continue;
            }
            String attr = key.substring(prefixLen);
            if(!HEADER.contains(attr)) {
                user.setAttribute(attr, getHeader(request, key));
            }
        }
        return user;
    }

    public static String getHeader(HttpServletRequest request, String key) {
        String input = request.getHeader(key);
        if(input == null) {
            return null;
        }
        // Some values can be hex-encoded -> translate to "human readable"
        if(!input.startsWith("0x")) {
            return input;
        }
        input = input.substring(2);
        byte[] bytes = DatatypeConverter.parseHexBinary(input);
        return new String(bytes, Charset.forName("UTF-8"));
    }

}
