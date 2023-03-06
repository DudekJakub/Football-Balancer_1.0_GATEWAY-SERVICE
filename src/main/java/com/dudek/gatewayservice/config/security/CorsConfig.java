package com.dudek.gatewayservice.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
public class CorsConfig {

    @Bean
    public WebFilter corsFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            HttpHeaders httpHeaders = exchange.getResponse().getHeaders();

            if (CorsUtils.isPreFlightRequest(request)) {
                httpHeaders.add("Access-Control-Allow-Origin", "*");
            }
            if (CorsUtils.isCorsRequest(request)) {
                if (request.getMethod() != HttpMethod.OPTIONS) {
                    httpHeaders.add("Access-Control-Allow-Methods", "GET, PUT, PATCH, POST, DELETE, OPTIONS");
                    httpHeaders.add("Access-Control-Max-Age", "3600");
                    httpHeaders.add("Access-Control-Allow-Headers", "Authorization, Content-Type");
                } else if (request.getMethod() == HttpMethod.OPTIONS) {
                    httpHeaders.add("Access-Control-Allow-Credentials", "true");
                    httpHeaders.add("Access-Control-Allow-Headers", "Authorization, Content-Type");
                    httpHeaders.add("Access-Control-Allow-Methods", "GET, PUT, PATCH, POST, DELETE, OPTIONS");
                    return Mono.empty();
                }
                return chain.filter(exchange);
            }
            return chain.filter(exchange);
        };
    }
}
