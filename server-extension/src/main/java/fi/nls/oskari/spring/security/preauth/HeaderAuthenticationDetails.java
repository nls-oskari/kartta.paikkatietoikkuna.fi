package fi.nls.oskari.spring.security.preauth;

import fi.nls.oskari.domain.User;
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
        // offloaded to helper in case WebAuthenticationDetails is saved to session
        this.user = UserDetailsHelper.parseUserFromHeaders(request, headerPrefix);
    }

    public User getUser() {
        return this.user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getGrantedAuthorities() {
        return this.grantedAuthorities;
    }
}