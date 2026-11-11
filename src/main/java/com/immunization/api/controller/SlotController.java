package com.immunization.api.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.immunization.api.dto.AppointmentSlotResponse;
import com.immunization.api.dto.OverrideRequest;
import com.immunization.api.entity.SlotOverride;
import com.immunization.api.entity.SlotTemplate;
import com.immunization.api.repository.SlotOverrideRepository;
import com.immunization.api.service.SlotGenerationService;
import com.immunization.api.service.SlotOverrideService;
import com.immunization.api.service.SlotService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;
    private final SlotOverrideService slotOverrideService;
    private final SlotOverrideRepository slotOverrideRepository;

    @GetMapping("/appointment-slots")
    public List<List<AppointmentSlotResponse>> getAvailableSlots(@RequestParam Long clinicId) {
        LocalDate today = LocalDate.now();
        return slotService.getAvailableSlots(
            clinicId,
            today,
            today.plusDays(SlotGenerationService.WINDOW_DAYS - 1));
    }

    @GetMapping("/appointment-slots/templates")
    public List<SlotTemplate> getSlotTemplates(@RequestParam Long clinicId) {
        return slotService.getSlotTemplates(clinicId);
    }

    @GetMapping("/overrides")
    public List<SlotOverride> getAllOverrides() {
        return slotOverrideRepository.findAll();
    }

    @PostMapping("/overrides")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "keycloak", scopes = {"openid", "profile", "email"})
    public SlotOverride createOverride(@RequestBody OverrideRequest overrideRequest) {
        return slotOverrideService.createOverride(overrideRequest);
    }

}
