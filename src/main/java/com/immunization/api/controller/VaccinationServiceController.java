package com.immunization.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.immunization.api.dto.VaccinationServiceRequest;
import com.immunization.api.entity.VaccinationService;
import com.immunization.api.service.VaccinationServiceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/vaccination-services")
public class VaccinationServiceController {

    private final VaccinationServiceService vaccinationServiceService;

    public VaccinationServiceController(VaccinationServiceService vaccinationServiceService) {
        this.vaccinationServiceService = vaccinationServiceService;
    }

    @GetMapping
    public ResponseEntity<List<VaccinationService>> getVaccinationServices() {
        return ResponseEntity.ok(vaccinationServiceService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a vaccination service (ADMIN only)", description = "Requires the ADMIN role in the immunization Keycloak realm.")
    @SecurityRequirement(name = "keycloak", scopes = {"openid", "profile", "email"})
    public ResponseEntity<VaccinationService> createVaccinationService(
            @Valid @RequestBody VaccinationServiceRequest request) {
        VaccinationService created = vaccinationServiceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
