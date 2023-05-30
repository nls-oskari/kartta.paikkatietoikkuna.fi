package fi.nls.oskari.spring.security.preauth;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.util.PropertyUtil;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.GrantedAuthoritiesContainer;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

public class HeaderAuthenticationDetails extends WebAuthenticationDetails
        implements GrantedAuthoritiesContainer {

    private static final long serialVersionUID = -5518158129223090223L;
    private User user;
    private Collection<GrantedAuthority> grantedAuthorities = AuthorityUtils.NO_AUTHORITIES;

    public HeaderAuthenticationDetails(HttpServletRequest request, String headerPrefix) {
        super(request);
        boolean devLogin = isDevEnv();
        if (devLogin) {
            this.user = generateDevUser();
        } else {
            // offloaded to helper in case WebAuthenticationDetails is saved to session
            this.user = UserDetailsHelper.parseUserFromHeaders(request, headerPrefix);
        }
    }


    public User getUser() {
        return this.user;
    }

    public static boolean isDevEnv() {
        return PropertyUtil.getOptional("useDevLogin", false);
    }
    private User generateDevUser() {
        User user = new User();
        user.setEmail(PropertyUtil.get("useDevLogin.email", "dev@oskari.org"));
        user.setFirstname(PropertyUtil.get("useDevLogin.firstname", "dev"));
        user.setLastname(PropertyUtil.get("useDevLogin.lastname", "oskari"));
        user.setScreenname(PropertyUtil.get("useDevLogin.screenname", "dev"));
        return user;
    }
    @Override
    public Collection<? extends GrantedAuthority> getGrantedAuthorities() {
        return this.grantedAuthorities;
    }
}