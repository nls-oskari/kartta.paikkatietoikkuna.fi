package fi.nls.oskari.spring.security.preauth;

import fi.nls.oskari.util.PropertyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Profile("preauth")
@Configuration
@EnableWebSecurity
@Order()
public class OskariPreAuthenticationSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // Don't set "X-Frame-Options: deny" header, that would prevent
        // embedded maps from working
        http.headers().frameOptions().disable();

        // Don't create unnecessary sessions
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);

        // Disable HSTS header, we don't want to force HTTPS for ALL requests
        http.headers().httpStrictTransportSecurity().disable();

        // 3rd party cookie blockers don't really work with cookie based CSRF protection on embedded maps.
        // Configure nginx to attach SameSite-flag to cookies instead.
        http.csrf().disable();

        OskariRequestHeaderAuthenticationFilter filter = new OskariRequestHeaderAuthenticationFilter();
        filter.setAuthenticationSuccessHandler(new OskariPreAuthenticationSuccessHandler());
        filter.setPrincipalRequestHeader(PropertyUtil.get("oskari.preauth.username.header", "auth-email"));

        HeaderAuthenticationDetailsSource headerAuthenticationDetailsSource = new HeaderAuthenticationDetailsSource();

        filter.setExceptionIfHeaderMissing(true);

        filter.setAuthenticationDetailsSource(headerAuthenticationDetailsSource);
        filter.setAuthenticationManager(authenticationManager());
        filter.setContinueFilterChainOnUnsuccessfulAuthentication(false);

        String authorizeUrl = PropertyUtil.get("oskari.authorize.url", "/auth");

        // use authorization for ALL requests
        http.authorizeRequests()
                // IF accessing /auth -> require authentication (== headers)
                .antMatchers(authorizeUrl).authenticated();

        // Add the preauth filter listening to /auth paths
        http
                .requestMatchers()
                .antMatchers(authorizeUrl)
                .and()
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .anyRequest().authenticated();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth)
            throws Exception {
        PreAuthenticatedAuthenticationProvider preAuthenticatedProvider = new PreAuthenticatedAuthenticationProvider();
        preAuthenticatedProvider.setPreAuthenticatedUserDetailsService(new OskariPreAuthenticatedUserDetailsService());
        auth.authenticationProvider(preAuthenticatedProvider);
    }
}