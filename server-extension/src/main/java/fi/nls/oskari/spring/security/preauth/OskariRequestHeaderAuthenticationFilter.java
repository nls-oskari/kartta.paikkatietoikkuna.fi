package fi.nls.oskari.spring.security.preauth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
   
}