package com.immunization.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.immunization.api.dto.UserProfileResponse;

@RestController
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get the authenticated user profile",
        description = "Requires a valid Keycloak access token. Click Authorize and sign in with username `immunizationadmin` and password `immunizationadmin`."
    )
    @SecurityRequirement(name = "keycloak", scopes = {"openid", "profile", "email"})
    public ResponseEntity<UserProfileResponse> userProfile(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        logger.info("Authenticated user: subject={}, username={}", jwt.getSubject(), username);
        return ResponseEntity.ok(new UserProfileResponse(jwt.getSubject(), username));
    }
}
