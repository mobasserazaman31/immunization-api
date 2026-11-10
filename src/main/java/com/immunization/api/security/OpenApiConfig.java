package com.immunization.api.security;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition
@SecurityScheme(
    name = "keycloak",
    type = SecuritySchemeType.OAUTH2,
    flows = @OAuthFlows(authorizationCode = @OAuthFlow(
        authorizationUrl = "http://localhost:8081/realms/immunization/protocol/openid-connect/auth?prompt=login",
        tokenUrl = "http://localhost:8081/realms/immunization/protocol/openid-connect/token",
        scopes = {
            @OAuthScope(name = "openid"),
            @OAuthScope(name = "profile"),
            @OAuthScope(name = "email")
        }
    ))
)
public class OpenApiConfig {
}
