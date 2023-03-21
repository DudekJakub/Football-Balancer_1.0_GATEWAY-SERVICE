package com.dudek.gatewayservice.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private static final String TOKEN_PREFIX = "Bearer ";
    private final Logger logger = LoggerFactory.getLogger(SecurityContextRepository.class);
    private final AuthenticationManager authenticationManager;

    public SecurityContextRepository(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String upgradeHeader = request.getHeaders().getFirst(HttpHeaders.UPGRADE);
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String authToken = null;

        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            authToken = authHeader.replace(TOKEN_PREFIX, "");
        } else if (upgradeHeader != null && Objects.equals(upgradeHeader, "websocket")) {
            authToken = request.getQueryParams().getFirst(TOKEN_PREFIX.replace(" ", ""));
        } else {
            logger.debug("Couldn't find bearer string from request [{}]. Will ignore the header!", request.getPath());
        }

        if (authToken != null) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(authToken, authToken);
            return this.authenticationManager.authenticate(authentication).map(SecurityContextImpl::new);
        } else {
            return Mono.empty();
        }
    }
}
