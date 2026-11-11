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
import com.immunization.api.repository.SlotRepository;
import java.time.LocalDate;

@Service
public class SlotOverrideService {

    private final SlotOverrideRepository slotOverrideRepository;
    private final ClinicRepository clinicRepository;
    private final SlotRepository slotRepository;

    SlotOverrideService(
            SlotOverrideRepository slotOverrideRepository,
            ClinicRepository clinicRepository,
            SlotRepository slotRepository) {
        this.slotOverrideRepository = slotOverrideRepository;
        this.clinicRepository = clinicRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional
    public SlotOverride createOverride(OverrideRequest overrideRequest) {
        LocalDate latestAllowedDate = LocalDate.now().plusDays(SlotGenerationService.WINDOW_DAYS);
        if (overrideRequest.date() == null || !overrideRequest.date().isAfter(latestAllowedDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Overrides must be created more than 7 days in advance");
        }

        Clinic clinic = clinicRepository.findById(overrideRequest.clinicId())
                .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));

        LocalDate date = overrideRequest.date();
        if (slotRepository.existsByClinic_IdAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                clinic.getId(),
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay())) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Slots have already been generated for the override date");
        }

        SlotOverride slotOverride = new SlotOverride();
        slotOverride.setClinic(clinic);
        slotOverride.setOverrideDate(overrideRequest.date());
        slotOverride.setClosed(overrideRequest.closed());
        slotOverride.setStartTime(overrideRequest.startTime());
        slotOverride.setEndTime(overrideRequest.endTime());
        slotOverride.setReason(overrideRequest.reason());
        slotOverride.setCapacity(overrideRequest.capacity());
        return slotOverrideRepository.save(slotOverride);
    }
}
