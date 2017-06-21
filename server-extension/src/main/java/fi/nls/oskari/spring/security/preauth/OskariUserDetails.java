package fi.nls.oskari.spring.security.preauth;

import java.util.Collection;

import fi.nls.oskari.domain.User;
import org.springframework.security.core.GrantedAuthority;

public class OskariUserDetails extends org.springframework.security.core.userdetails.User {

    private static final long serialVersionUID = -6421004319053775020L;

    private User user;

    public OskariUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getEmail(), "N/A", true, true, true, true, authorities);
        this.user = user;
    }
    public User getUser() {
        return user;
    }
}