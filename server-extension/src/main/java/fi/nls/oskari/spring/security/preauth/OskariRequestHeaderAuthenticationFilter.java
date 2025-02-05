package fi.nls.oskari.spring.security.preauth;

import java.io.IOException;

import fi.nls.oskari.util.PropertyUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

public class OskariRequestHeaderAuthenticationFilter extends RequestHeaderAuthenticationFilter {
    
    private AuthenticationSuccessHandler successHandler;
        
    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }
    
    /**
     * Puts the <code>Authentication</code> instance returned by the authentication
     * manager into the secure context.
     * @throws ServletException 
     * @throws IOException 
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
            HttpServletResponse response, Authentication authResult) {
        try {
            super.successfulAuthentication(request, response, authResult);
            if (this.successHandler != null) {
                this.successHandler.onAuthenticationSuccess(request, response,  authResult);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String authorizeUrl = PropertyUtil.get("oskari.authorize.url", "/auth");

        if (request.getRequestURI().equals(authorizeUrl)) {
            return new HeaderAuthenticationDetails(request, HeaderAuthenticationDetailsSource.getHeaderPrexif());
        }
        return null; // No authentication for other paths
    }
    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A"; // No password needed
    }

}