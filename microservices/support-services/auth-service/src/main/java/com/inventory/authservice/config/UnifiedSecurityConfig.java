package com.inventory.authservice.config;

import com.inventory.authservice.security.UsernamePasswordAuthenticationFilter;
import com.inventory.authservice.security.handler.CustomAuthenticationEntryPoint;
import com.inventory.authservice.security.handler.CustomLogoutHandler;
import com.inventory.authservice.security.handler.CustomLogoutSuccessHandler;
import com.inventory.authservice.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
@SuppressWarnings("unused")
public class UnifiedSecurityConfig {

    private static final int BCRYPT_STRENGTH = 12;
    private static final int MAX_SESSIONS_PER_USER = 3;
    @SuppressWarnings("unused")
    private static final int SESSION_TIMEOUT_SECONDS = 1800;

    @Value("${security.cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String allowedOrigins;

    @Value("${security.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String allowedMethods;

    @Value("${security.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${security.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${security.cors.max-age:3600}")
    private long corsMaxAge;

    private final UserDetailsServiceImpl userDetailsService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomLogoutHandler customLogoutHandler;
    private final CustomLogoutSuccessHandler logoutSuccessHandler;
    private final UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter;

    public UnifiedSecurityConfig(
            UserDetailsServiceImpl userDetailsService,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomLogoutHandler customLogoutHandler,
            CustomLogoutSuccessHandler logoutSuccessHandler,
            UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.customLogoutHandler = customLogoutHandler;
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.usernamePasswordAuthenticationFilter = usernamePasswordAuthenticationFilter;
    }

    @Bean
    @Primary
    public SecurityFilterChain unifiedSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/api/**", "/auth/**", "/login/**", "/logout/**")
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(MAX_SESSIONS_PER_USER)
                .sessionRegistry(sessionRegistry())
                .maxSessionsPreventsLogin(false)
            )
            .sessionManagement(session -> session
                .sessionFixation(sf -> sf.migrateSession())
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/register",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/forgot-password",
                    "/api/v1/auth/reset-password",
                    "/api/v1/auth/verify-email",
                    "/api/v1/auth/mfa/setup",
                    "/api/v1/auth/mfa/verify",
                    "/actuator/health",
                    "/actuator/info",
                    "/actuator/prometheus",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/webjars/**",
                    "/error"
                ).permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/manager/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/operator/**").hasAnyRole("ADMIN", "MANAGER", "OPERATOR")
                .requestMatchers("/api/user/**").authenticated()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(usernamePasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .addLogoutHandler(customLogoutHandler)
                .logoutSuccessHandler(logoutSuccessHandler)
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me", "access-token", "refresh-token")
            )
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self' data:; " +
                        "connect-src 'self' https:; " +
                        "frame-ancestors 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'"
                    )
                )
                .xssProtection(xss -> xss
                    .headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
                .contentTypeOptions(cto -> cto.disable())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                    .preload(true)
                )
                .frameOptions(frame -> frame.deny())
            )
            .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);
        
        if ("*".equals(allowedHeaders)) {
            configuration.addAllowedHeader("*");
        } else {
            List<String> headers = Arrays.asList(allowedHeaders.split(","));
            configuration.setAllowedHeaders(headers);
        }
        
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(corsMaxAge);
        
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Refresh-Token",
            "X-Request-ID",
            "X-RateLimit-Limit",
            "X-RateLimit-Remaining",
            "X-RateLimit-Reset"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
