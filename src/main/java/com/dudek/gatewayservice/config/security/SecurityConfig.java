package com.dudek.gatewayservice.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository;

    @Autowired
    public SecurityConfig(final AuthenticationManager authenticationManager, final SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint((exchange, ex) -> Mono.fromRunnable(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                })).accessDeniedHandler((exchange, denied) -> Mono.fromRunnable(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                })).and()
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange()
                .pathMatchers("/auth/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/room/**").permitAll()
                .pathMatchers("/webjars/**").permitAll()
                .pathMatchers("/swagger-ui.html/**").permitAll()
                .pathMatchers("/v3/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/notification/stream/**").permitAll()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/**").authenticated()
                .anyExchange().authenticated();

        return http.build();
    }
}
