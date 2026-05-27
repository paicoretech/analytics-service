package com.paic.nbm.analyticsservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class OptionsGlobalWebFilter {

    @Bean
    public WebFilter handlePreflight() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
                System.out.println("[OPTIONS Handler] Responding to preflight for: " + exchange.getRequest().getURI());
                exchange.getResponse().setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }
            return chain.filter(exchange);
        };
    }
}

