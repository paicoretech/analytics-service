package com.paic.nbm.analyticsservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Global CORS configuration for WebFlux.
 * Handles CORS preflight (OPTIONS) and allows cross-origin requests.
 */
@Configuration
public class WebFluxCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(false);
        config.addAllowedOriginPattern("*");// You could restrict to specific origins in production
        config.addAllowedMethod("*"); // GET, POST, OPTIONS, etc.
        config.addAllowedHeader("*"); // Content-Type, Authorization, etc.
// Set true if you need to allow cookies

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Apply to all routes

        return new CorsWebFilter(source);
    }
}
