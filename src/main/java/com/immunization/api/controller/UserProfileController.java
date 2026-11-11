package com.immunization.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "keycloak", scopes = {"openid", "profile", "email"})
    public String userProfile(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        logger.info("Authenticated user: subject={}, username={}", jwt.getSubject(), username);
        return "Authenticated user: " + username;
    }
}
