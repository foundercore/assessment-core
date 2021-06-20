package com.assessment.iam.configurations;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.session.data.mongo.JdkMongoSessionConverter;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;

@Configuration
@EnableMongoHttpSession
@EnableWebSecurity
public class CustomWebSecurityConfigurerAdapter extends AbstractHttpSessionApplicationInitializer {

    @Bean
    public HttpSessionIdResolver webSessionIdResolver() {
        HeaderHttpSessionIdResolver resolver = new HeaderHttpSessionIdResolver("x-auth-token");
        return resolver;
    }

    @Primary
    @Bean
    public JdkMongoSessionConverter jdkMongoSessionConverter() {
        return new JdkMongoSessionConverter(Duration.ofHours(1));
    }

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
    public static class AppWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {


        @Lazy
        @Autowired
        @Qualifier("userDetailsService")
        private UserDetailsService userDetailsService;


        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
            DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
            authProvider.setUserDetailsService(userDetailsService);
            authProvider.setPasswordEncoder(passwordEncoder());
            return authProvider;
        }

        @Bean("authenticationManager")
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder(4);
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(authenticationProvider());
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            WebSecurity.IgnoredRequestConfigurer ignoredRequestConfigurer = web.ignoring()
                    .antMatchers("/resources/**");
        }


        @Bean
        CorsConfigurationSource corsConfigurationSource() {
            final CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(ImmutableList.of("*"));
            //configuration.setAllowedOrigins(ImmutableList.of("http://localhost:4200", "http://107.22.178.115:4200"));
            configuration.setAllowedMethods(ImmutableList.of("*"));
            // setAllowCredentials(true) is important, otherwise:
            // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
            configuration.setAllowCredentials(true);
            // setAllowedHeaders is important! Without it, OPTIONS preflight request
            // will fail with 403 Invalid CORS request
            configuration.setAllowedHeaders(ImmutableList.of("*"));
            configuration.setExposedHeaders(ImmutableList.of("x-auth-token", "X-XSRF-TOKEN"));
            final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.cors().disable().authorizeRequests()
                    .antMatchers("/public/**", "/error**", "/actuator**",
                            "/favicon.ico", "/v2/api-docs", "/configuration/**",
                            "/swagger-resources/**", "/swagger-ui.html**",
                            "/webjars/**", "/login", "/logout", "/api-docs/**").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .logout().permitAll()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                    .and()
                    .httpBasic()
                    .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .and()
                    .csrf().disable();
            ;
        }

        @Bean(name = "sessionRegistry")
        public SessionRegistry sessionRegistry() {
            sessionRegistry = new SessionRegistryImpl();
            return sessionRegistry;
        }

        @Bean
        public ServletListenerRegistrationBean httpSessionEventPublisher() {
            return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
        }

        @Autowired
        private static SessionRegistry sessionRegistry;

        public static void expireUserSessions(String username) {

            for (Object principal : sessionRegistry.getAllPrincipals()) {
                if (principal instanceof SecurityProperties.User) {
                    UserDetails userDetails = (UserDetails) principal;
                    if (userDetails.getUsername().equals(username)) {
                        sessionRegistry.getAllSessions(userDetails, false).forEach(SessionInformation::expireNow);
                    }
                }
            }
        }
    }
}