package com.dudek.gatewayservice.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.SwaggerUiConfigParameters;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class OpenApiConfig {

    @Bean
    public CommandLineRunner openApiGroups(RouteDefinitionLocator locator, SwaggerUiConfigParameters swaggerUiConfigParameters) {
        return args -> Objects.requireNonNull(locator
                        .getRouteDefinitions().collectList().block())
                .stream()
                .map(RouteDefinition::getId)
                .filter(id -> id.matches(".*-service"))
                .map(id -> {
                    String extractedServiceName = id.substring(id.lastIndexOf("/") + 1);
                    String idWithoutSuffix = id.replace(extractedServiceName, "");
                    return extractedServiceName + idWithoutSuffix;
                })
                .forEach(swaggerUiConfigParameters::addGroup);
    }

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Spring Cloud Gateway Service API")
                        .description("This API provides aggregated single entry point for all microservices.")
                        .version("0.5.0")
                        .contact(new Contact()
                                .name("Jakub Dudek")
                                .email("jakub.dudek94@gmail.com")));
    }
}
