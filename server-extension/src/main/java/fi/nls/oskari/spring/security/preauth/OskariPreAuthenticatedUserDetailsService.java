package fi.nls.oskari.spring.security.preauth;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesUserDetailsService;

public class OskariPreAuthenticatedUserDetailsService
        extends PreAuthenticatedGrantedAuthoritiesUserDetailsService {

    /**
     * Creates the final <tt>UserDetails</tt> object. Can be overridden to
     * customize the contents.
     *
     * @param token
     *            the authentication request token
     * @param authorities
     *            the pre-authenticated authorities.
     */
    @Override
    protected UserDetails createUserDetails(Authentication token,
            Collection<? extends GrantedAuthority> authorities) {

        Object details = token.getDetails();
        if (details instanceof HeaderAuthenticationDetails) {
            OskariUserDetails user = new OskariUserDetails(
                    ((HeaderAuthenticationDetails) details).getUser(), authorities);
            return user;
        }
        throw new RuntimeException("Unexpected user details");
    }
  
}