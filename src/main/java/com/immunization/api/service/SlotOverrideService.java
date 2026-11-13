package com.immunization.api.service;

import com.immunization.api.repository.ClinicRepository;
import com.immunization.api.repository.SlotOverrideRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.immunization.api.dto.OverrideRequest;
import com.immunization.api.entity.Clinic;
import com.immunization.api.entity.SlotOverride;
import java.time.LocalDate;

@Service
public class SlotOverrideService {

    private final SlotOverrideRepository slotOverrideRepository;
    private final ClinicRepository clinicRepository;
    private final SlotGenerationService slotGenerationService;

    SlotOverrideService(
            SlotOverrideRepository slotOverrideRepository,
            ClinicRepository clinicRepository,
            SlotGenerationService slotGenerationService) {
        this.slotOverrideRepository = slotOverrideRepository;
        this.clinicRepository = clinicRepository;
        this.slotGenerationService = slotGenerationService;
    }

    @Transactional
    public SlotOverride createOverride(OverrideRequest overrideRequest) {
        if (overrideRequest.date() == null || !overrideRequest.date().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                "Overrides can only be created for a future date");
        }

        Long clinicId = overrideRequest.clinicId();
        Clinic clinic = (clinicId == null ? java.util.Optional.<Clinic>empty() : clinicRepository.findById(clinicId))
                .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));

        SlotOverride slotOverride = slotOverrideRepository
            .findByClinicIdAndOverrideDate(clinic.getId(), overrideRequest.date())
            .orElseGet(SlotOverride::new);
        slotOverride.setClinic(clinic);
        slotOverride.setOverrideDate(overrideRequest.date());
        slotOverride.setClosed(overrideRequest.closed());
        slotOverride.setStartTime(overrideRequest.startTime());
        slotOverride.setEndTime(overrideRequest.endTime());
        slotOverride.setReason(overrideRequest.reason());
        slotOverride.setCapacity(overrideRequest.capacity());
        SlotOverride savedOverride = slotOverrideRepository.save(slotOverride);
        slotGenerationService.regenerateDate(clinic.getId(), overrideRequest.date());
        return savedOverride;
    }
}
