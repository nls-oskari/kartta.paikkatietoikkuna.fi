package fi.nls.oskari.spring;

import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.util.Locale;

/**
 * Initializes resources needed for Oskari and sorts HandlerMethodArgumentResolvers so the
 * custom extension (OskariParamMethodArgumentResolver) is the first on the list to be processed.
 */
@Configuration
@EnableWebMvc
@ComponentScan(
        excludeFilters = @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value={SpringConfig.class}),
        basePackages="fi.nls.oskari")
public class SpringConfig extends WebMvcConfigurerAdapter{

    //  --------- locale handling -------------
    // TODO: use this instead fi.nls.oskari.servlet.WebLocaleResolver
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
        registry.addInterceptor(new OskariRequestInterceptor());
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor(){
        LocaleChangeInterceptor localeChangeInterceptor=new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        return localeChangeInterceptor;
    }

    @Bean(name = "localeResolver")
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(new Locale("fi"));
        resolver.setCookieMaxAge(-1);
        resolver.setCookieName("oskari.language");
        return resolver;
    }
    //  --------- /locale handling -------------


    @Bean
    public ViewResolver getDefaultsViewResolver() {
        final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        // fallback to default JSP in spring-map
        resolver.setPrefix("/WEB-INF/jsp/");
        resolver.setSuffix(".jsp");
        resolver.setOrder(999999);
        return resolver;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // check for extension first, then the defaults
        messageSource.setBasenames("classpath:locale/messages-ext", "classpath:locale/messages");
        // if true, the key of the message will be displayed if the key is not
        // found, instead of throwing a NoSuchMessageException
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setFallbackToSystemLocale(false);
        //messageSource.setDefaultEncoding("UTF-8");
        // # -1 : never reload, 0 always reload
        messageSource.setCacheSeconds(-1);

        return messageSource;
    }

}
