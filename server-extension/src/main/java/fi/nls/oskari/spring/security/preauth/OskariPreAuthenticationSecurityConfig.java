package fi.nls.oskari.spring.security.preauth;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.security.database.OskariAuthenticationProvider;
import fi.nls.oskari.spring.security.database.OskariAuthenticationSuccessHandler;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.spring.SpringEnvHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

@Profile("preauth")
@Configuration
@EnableWebSecurity
// @Order()
public class OskariPreAuthenticationSecurityConfig {

    private static final Logger log = LogFactory.getLogger(OskariPreAuthenticationSecurityConfig.class);
    private final SpringEnvHelper env;
    private final OskariAuthenticationProvider oskariAuthenticationProvider;
    private final OskariAuthenticationSuccessHandler oskariAuthenticationSuccessHandler;

    public OskariPreAuthenticationSecurityConfig(SpringEnvHelper env,
                                        OskariAuthenticationProvider oskariAuthenticationProvider,
                                        OskariAuthenticationSuccessHandler oskariAuthenticationSuccessHandler) {
        this.env = env;
        this.oskariAuthenticationProvider = oskariAuthenticationProvider;
        this.oskariAuthenticationSuccessHandler = oskariAuthenticationSuccessHandler;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring preauth login");

        // Add custom authentication provider
        http.authenticationProvider(oskariAuthenticationProvider);

        // Disable frame options and CSRF for embedded maps
        http.headers(headers -> headers.frameOptions().disable());


        // Don't create unnecessary sessions
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);

        // Disable HSTS header, we don't want to force HTTPS for ALL requests
        http.headers().httpStrictTransportSecurity().disable();

        // 3rd party cookie blockers don't really work with cookie based CSRF protection on embedded maps.
        // Configure nginx to attach SameSite-flag to cookies instead.
        http.csrf(csrf -> csrf.disable());


        OskariRequestHeaderAuthenticationFilter filter = new OskariRequestHeaderAuthenticationFilter();
        filter.setAuthenticationSuccessHandler(new OskariPreAuthenticationSuccessHandler());
        filter.setPrincipalRequestHeader(PropertyUtil.get("oskari.preauth.username.header", "auth-email"));

        HeaderAuthenticationDetailsSource headerAuthenticationDetailsSource = new HeaderAuthenticationDetailsSource();
        boolean isDevEnv = HeaderAuthenticationDetails.isDevEnv();
        filter.setExceptionIfHeaderMissing(!isDevEnv);

        filter.setAuthenticationDetailsSource(headerAuthenticationDetailsSource);
        // commented out
        //filter.setAuthenticationManager(authenticationManager());
        filter.setContinueFilterChainOnUnsuccessfulAuthentication(isDevEnv);

        //http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());

        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        String authorizeUrl = PropertyUtil.get("oskari.authorize.url", "/auth");
        http.authorizeHttpRequests((authz) -> authz
                .requestMatchers(authorizeUrl).authenticated()
        );
        http.authorizeHttpRequests((authz) -> authz
                .requestMatchers(authorizeUrl)
                .authenticated()
        );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth)
            throws Exception {
        PreAuthenticatedAuthenticationProvider preAuthenticatedProvider = new PreAuthenticatedAuthenticationProvider();
        preAuthenticatedProvider.setPreAuthenticatedUserDetailsService(new OskariPreAuthenticatedUserDetailsService());
        auth.authenticationProvider(preAuthenticatedProvider);
    }
}