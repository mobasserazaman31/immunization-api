package com.immunization.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.immunization.api.dto.ClinicRequest;
import com.immunization.api.entity.Clinic;
import com.immunization.api.service.ClinicService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/clinics")
public class ClinicController {

    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @GetMapping
    public ResponseEntity<List<Clinic>> getClinics() {
        return ResponseEntity.ok(clinicService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a clinic (ADMIN only)", description = "Requires the ADMIN role in the immunization Keycloak realm.")
    @SecurityRequirement(name = "keycloak", scopes = {"openid", "profile", "email"})
    public ResponseEntity<Clinic> createClinic(@Valid @RequestBody ClinicRequest request) {
        Clinic created = clinicService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
