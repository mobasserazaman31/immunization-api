package com.immunization.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.immunization.api.entity.Clinic;
import com.immunization.api.entity.SlotOverride;
import com.immunization.api.entity.SlotTemplate;
import com.immunization.api.repository.ClinicRepository;
import com.immunization.api.repository.SlotRepository;
import com.immunization.api.repository.SlotOverrideRepository;
import com.immunization.api.repository.SlotTemplateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlotGenerationService {

    public static final int WINDOW_DAYS = 7;
    private static final int SLOT_DURATION_MINUTES = 30;

    private final ClinicRepository clinicRepository;
    private final SlotTemplateRepository templateRepository;
    private final SlotOverrideRepository overrideRepository;
    private final SlotRepository slotRepository;

    @Transactional
    public void populateRollingWindow() {
        LocalDate firstDate = LocalDate.now();
        LocalDate lastDate = firstDate.plusDays(WINDOW_DAYS - 1);
        for (Clinic clinic : clinicRepository.findByActiveTrue()) {
            generateClinicDates(clinic, firstDate, lastDate);
        }
    }

    @Transactional
    public void generateNewRollingDay() {
        LocalDate newWindowDate = LocalDate.now().plusDays(WINDOW_DAYS - 1);
        for (Clinic clinic : clinicRepository.findByActiveTrue()) {
            generateClinicDates(clinic, newWindowDate, newWindowDate);
        }
    }

    private void generateClinicDates(Clinic clinic, LocalDate firstDate, LocalDate lastDate) {
        Long clinicId = clinic.getId();
        Map<java.time.DayOfWeek, SlotTemplate> templates = templateRepository
                .findByClinicIdAndActiveTrue(clinicId)
                .stream()
                .collect(Collectors.toMap(SlotTemplate::getDayOfWeek, Function.identity()));
        Map<LocalDate, SlotOverride> overrides = overrideRepository
                .findByClinicIdAndOverrideDateBetween(clinicId, firstDate, lastDate)
                .stream()
                .collect(Collectors.toMap(SlotOverride::getOverrideDate, Function.identity()));

        for (LocalDate date = firstDate; !date.isAfter(lastDate); date = date.plusDays(1)) {
            SlotOverride override = overrides.get(date);
            if (override != null && override.isClosed()) {
                continue;
            }

            SlotTemplate template = templates.get(date.getDayOfWeek());
            String start = override != null ? override.getStartTime()
                    : template != null ? template.getStartTime() : null;
            String end = override != null ? override.getEndTime()
                    : template != null ? template.getEndTime() : null;
            if (start == null || end == null) {
                continue;
            }

            int capacity = override != null ? override.getCapacity() : template.getCapacity();
            LocalTime openingTime = LocalTime.parse(start);
            LocalTime closingTime = LocalTime.parse(end);

            for (LocalTime time = openingTime;
                    !time.plusMinutes(SLOT_DURATION_MINUTES).isAfter(closingTime);
                    time = time.plusMinutes(SLOT_DURATION_MINUTES)) {
                LocalDateTime slotStart = LocalDateTime.of(date, time);
                slotRepository.insertIfAbsent(
                        clinicId,
                        slotStart,
                        slotStart.plusMinutes(SLOT_DURATION_MINUTES),
                        capacity);
            }
        }
    }
}
