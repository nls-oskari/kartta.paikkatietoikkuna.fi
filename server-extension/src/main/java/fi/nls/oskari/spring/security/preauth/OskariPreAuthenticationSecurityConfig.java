package fi.nls.oskari.spring.security.preauth;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.spring.SpringEnvHelper;
import org.oskari.spring.security.OskariSpringSecurityDsl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

import java.util.Arrays;
import java.util.UUID;

@Profile("preauth")
@Configuration
@EnableWebSecurity(debug = true)
public class OskariPreAuthenticationSecurityConfig {

    private static final Logger log = LogFactory.getLogger(OskariPreAuthenticationSecurityConfig.class);
    private final SpringEnvHelper env;

    @Autowired
    public OskariPreAuthenticationSecurityConfig(SpringEnvHelper env) {
        this.env = env;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring preauth login");

        // Add custom authentication provider
        PreAuthenticatedAuthenticationProvider preAuthProvider = new PreAuthenticatedAuthenticationProvider();
        preAuthProvider.setPreAuthenticatedUserDetailsService(new OskariPreAuthenticatedUserDetailsService());
        http.authenticationProvider(preAuthProvider);

        // The filter that does the login
        String authorizeUrl = PropertyUtil.get("oskari.authorize.url", "/auth");
        OskariRequestHeaderAuthenticationFilter filter = new OskariRequestHeaderAuthenticationFilter();
        filter.setAuthenticationSuccessHandler(new OskariPreAuthenticationSuccessHandler());
        // looks like we need to pass HeaderAuthenticationDetailsSource or Spring throws a tantrum
        // even when filter.getPreAuthenticatedPrincipal() does the same thing
        filter.setAuthenticationDetailsSource(new HeaderAuthenticationDetailsSource());
        filter.setAuthPath(authorizeUrl);
        filter.setAuthenticationManager(authenticationManager(preAuthProvider));

        boolean isDevEnv = HeaderAuthenticationDetails.isDevEnv();
        filter.setExceptionIfHeaderMissing(!isDevEnv);
        filter.setContinueFilterChainOnUnsuccessfulAuthentication(isDevEnv);

        http
                .authorizeHttpRequests(authorize -> authorize
                    // the pre-auth endpoint requires authenticated user (pre-auth headers to be sent)
                    .requestMatchers(authorizeUrl).authenticated()
                    // any other path is free for all
                    .anyRequest().permitAll());
        // Use with defaults
        http.with(OskariSpringSecurityDsl.oskariCommonDsl(),
                (dsl) -> dsl
                        .setAllowMapsToBeEmbedded(true)
                        .setLogoutUrl(env.getLogoutUrl())
                        .setLogoutSuccessUrl(env.getMapUrl())
                        .setPreAuthFilter(filter)
                        .setDisableUnnecessarySessions(false)
        );

        return http.build();
    }

    public AuthenticationManager authenticationManager(PreAuthenticatedAuthenticationProvider preAuthProvider) {
        AnonymousAuthenticationProvider guestUserProvider = new AnonymousAuthenticationProvider(UUID.randomUUID().toString());
        return new ProviderManager(Arrays.asList(guestUserProvider, preAuthProvider));
    }
}