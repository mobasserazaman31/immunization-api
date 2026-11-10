package com.immunization.api.service;

import com.immunization.api.repository.ClinicRepository;
import com.immunization.api.repository.SlotOverrideRepository;
import org.springframework.stereotype.Service;

import com.immunization.api.dto.OverrideRequest;
import com.immunization.api.entity.Clinic;
import com.immunization.api.entity.SlotOverride;

@Service
public class SlotOverrideService {

    private final SlotOverrideRepository slotOverrideRepository;
    private final ClinicRepository clinicRepository;

    SlotOverrideService(SlotOverrideRepository slotOverrideRepository, ClinicRepository clinicRepository) {
        this.slotOverrideRepository = slotOverrideRepository;
        this.clinicRepository = clinicRepository;
    }

    public SlotOverride createOverride(OverrideRequest overrideRequest) {
        Clinic clinic = clinicRepository.findById(overrideRequest.clinicId())
                .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));

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
