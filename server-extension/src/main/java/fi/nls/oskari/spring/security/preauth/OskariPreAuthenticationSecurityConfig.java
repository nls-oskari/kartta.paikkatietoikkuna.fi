package fi.nls.oskari.spring.security.preauth;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.spring.SpringEnvHelper;
import org.oskari.spring.security.OskariLoginFailureHandler;
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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
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



        OskariRequestHeaderAuthenticationFilter filter = new OskariRequestHeaderAuthenticationFilter();
        filter.setAuthenticationSuccessHandler(new OskariPreAuthenticationSuccessHandler());
        filter.setPrincipalRequestHeader(PropertyUtil.get("oskari.preauth.username.header", "auth-email"));

        HeaderAuthenticationDetailsSource headerAuthenticationDetailsSource = new HeaderAuthenticationDetailsSource();
        boolean isDevEnv = HeaderAuthenticationDetails.isDevEnv();
        filter.setExceptionIfHeaderMissing(!isDevEnv);

        filter.setAuthenticationDetailsSource(headerAuthenticationDetailsSource);
        filter.setContinueFilterChainOnUnsuccessfulAuthentication(isDevEnv);
        //OskariSpringSecurityDsl oskariCommonDsl = OskariSpringSecurityDsl.oskariCommonDsl();
        // feels like this should come from Spring and not somehing we inject, but...
        filter.setAuthenticationManager(authenticationManager());

        String authorizeUrl = PropertyUtil.get("oskari.authorize.url", "/auth");

        //http.headers((headers) -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
        http
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(authorizeUrl).authenticated()
                //.authorizeHttpRequests(authorize -> authorize
                    .anyRequest().permitAll())
                .addFilterBefore(filter, AbstractPreAuthenticatedProcessingFilter.class)
                .formLogin(form -> form
                        .loginPage("/") // just so we can give the geoportal as page to go after logging out
                        .permitAll()
                );
        OskariSpringSecurityDsl.disableFrameOptions(http);
        OskariSpringSecurityDsl.disableCSRF(http);
        OskariSpringSecurityDsl.disableHSTS(http);
        OskariSpringSecurityDsl.configLogout(http, "/logout", "/");
        // OskariSpringSecurityDsl.disableUnnecessarySessions(http);
        /*
        http.with(oskariCommonDsl,
                (dsl) -> dsl
                        .setLogoutUrl(env.getLogoutUrl())
                        .setLogoutSuccessUrl(env.getLoggedOutPage())
                        //.setLoginFilter(filter)
                        .setUseCommonLogout(false)
                        .setDisableUnnecessarySessions(false)
        );
        oskariCommonDsl.configLogout(http);
         */
        return http.build();
    }

    public AuthenticationManager authenticationManager() {
        PreAuthenticatedAuthenticationProvider preAuthenticatedProvider = new PreAuthenticatedAuthenticationProvider();
        preAuthenticatedProvider.setPreAuthenticatedUserDetailsService(new OskariPreAuthenticatedUserDetailsService());

        AnonymousAuthenticationProvider guestUserProvider = new AnonymousAuthenticationProvider(UUID.randomUUID().toString());
        return new ProviderManager(Arrays.asList(guestUserProvider, preAuthenticatedProvider));
    }
}