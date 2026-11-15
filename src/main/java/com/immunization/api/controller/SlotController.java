package com.immunization.api.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.immunization.api.dto.AppointmentSlotResponse;
import com.immunization.api.dto.OverrideRequest;
import com.immunization.api.dto.SlotTemplateRequest;
import com.immunization.api.entity.SlotOverride;
import com.immunization.api.entity.SlotTemplate;
import com.immunization.api.repository.SlotOverrideRepository;
import com.immunization.api.service.SlotGenerationService;
import com.immunization.api.service.SlotOverrideService;
import com.immunization.api.service.SlotService;
import com.immunization.api.service.SlotTemplateService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;
    private final SlotOverrideService slotOverrideService;
    private final SlotOverrideRepository slotOverrideRepository;
    private final SlotTemplateService slotTemplateService;

    @GetMapping("/appointment-slots")
    public ResponseEntity<List<List<AppointmentSlotResponse>>> getAvailableSlots(@RequestParam Long clinicId) {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(slotService.getAvailableSlots(
                clinicId,
                today,
                today.plusDays(SlotGenerationService.WINDOW_DAYS - 1)));
    }

    @GetMapping("/appointment-slots/templates")
    public ResponseEntity<List<SlotTemplate>> getSlotTemplates(@RequestParam Long clinicId) {
        return ResponseEntity.ok(slotService.getSlotTemplates(clinicId));
    }

    @PostMapping("/appointment-slots/templates")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a slot template (ADMIN only)", description = "Requires the ADMIN role in the immunization Keycloak realm.")
    @SecurityRequirement(name = "keycloak", scopes = {"openid", "profile", "email"})
    public ResponseEntity<SlotTemplate> createSlotTemplate(@Valid @RequestBody SlotTemplateRequest request) {
        SlotTemplate created = slotTemplateService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/overrides")
    public ResponseEntity<List<SlotOverride>> getAllOverrides() {
        return ResponseEntity.ok(slotOverrideRepository.findAll());
    }

    @PostMapping("/overrides")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create a slot override (ADMIN only)",
        description = "Requires the ADMIN role in the immunization Keycloak realm. Click Authorize and sign in with username `immunizationadmin` and password `immunizationadmin`."
    )
    @SecurityRequirement(name = "keycloak", scopes = {"openid", "profile", "email"})
    public ResponseEntity<SlotOverride> createOverride(@RequestBody OverrideRequest overrideRequest) {
        SlotOverride created = slotOverrideService.createOverride(overrideRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/overrides/{overrideId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel a slot override (ADMIN only)", description = "Deactivates the override, restores the template schedule, and cancels and logs affected appointments.")
    @SecurityRequirement(name = "keycloak", scopes = {"openid", "profile", "email"})
    public ResponseEntity<Void> cancelOverride(@PathVariable Long overrideId) {
        slotOverrideService.cancelOverride(overrideId);
        return ResponseEntity.noContent().build();
    }

}
