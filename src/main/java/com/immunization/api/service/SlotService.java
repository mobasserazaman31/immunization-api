package com.immunization.api.service;

import com.immunization.api.dto.AppointmentSlotResponse;
import com.immunization.api.entity.Slot;
import com.immunization.api.entity.SlotStatus;
import com.immunization.api.entity.SlotTemplate;
import com.immunization.api.repository.SlotRepository;
import com.immunization.api.repository.SlotTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotTemplateRepository templateRepository;
    private final SlotRepository slotRepository;

    public List<List<AppointmentSlotResponse>> getAvailableSlots(
            Long clinicId,
            LocalDate startDate,
            LocalDate endDate) {

        Map<LocalDate, List<AppointmentSlotResponse>> slotsByDate = new LinkedHashMap<>();
        LocalDateTime startInclusive = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();
        List<Slot> availableSlots = slotRepository
                .findByClinic_IdAndStartTimeGreaterThanEqualAndStartTimeLessThanAndStatusAndActiveTrueOrderByStartTimeAsc(
                        clinicId,
                        startInclusive,
                        endExclusive,
                        SlotStatus.AVAILABLE);

        for (Slot slot : availableSlots) {
            LocalDate date = slot.getStartTime().toLocalDate();
            slotsByDate.computeIfAbsent(date, ignored -> new ArrayList<>())
                    .add(AppointmentSlotResponse.fromDateTime(slot.getStartTime()));
        }

        return new ArrayList<>(slotsByDate.values());
    }

    public List<SlotTemplate> getSlotTemplates(Long clinicId) {
        return templateRepository.findByClinicIdAndActiveTrue(clinicId);
    }
}