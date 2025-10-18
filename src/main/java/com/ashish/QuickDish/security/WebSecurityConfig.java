package com.ashish.QuickDish.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.List;

@Configuration
public class WebSecurityConfig {

    private final JWTAuthFilter jwtAuthFilter;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public WebSecurityConfig(JWTAuthFilter jwtAuthFilter, HandlerExceptionResolver handlerExceptionResolver) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    // Register AuthenticationManager for AuthService
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Main Security Filter Chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for REST API
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) // JWT filter
                .authorizeHttpRequests(auth -> auth
                        // Swagger UI and OpenAPI docs public
                        .requestMatchers(
                                "/api/v1/swagger-ui.html",
                                "/api/v1/swagger-ui/**",
                                "/api/v1/v3/api-docs/**",
                                "/api/v1/swagger-resources/**",
                                "/api/v1/webjars/**",
                                "/api/v1/auth/**"
                        ).permitAll()
                        // Admin endpoints
                        .requestMatchers("/admin/**").hasRole("RESTAURANT_OWNER")
                        // User endpoints
                        .requestMatchers("/orders/**", "/users/**").authenticated()
                        // Any other request
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler())); // Custom access denied handling

        return http.build();
    }

    // Access Denied Handler
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                handlerExceptionResolver.resolveException(request, response, null, accessDeniedException);
    }

    // CORS configuration
    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:5173")); // Frontend URL
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
