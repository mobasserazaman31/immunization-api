package com.immunization.api.service;

import com.immunization.api.dto.AppointmentSlotResponse;
import com.immunization.api.entity.*;
import com.immunization.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AppointmentSlotService {

    private final SlotTemplateRepository templateRepository;
    private final SlotOverrideRepository overrideRepository;

    public List<AppointmentSlotResponse> generateSlots(
            Long clinicId,
            LocalDate startDate,
            LocalDate endDate) {

        List<AppointmentSlotResponse> slots = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

            // 1. Check for date-specific override
            Optional<SlotOverride> override = overrideRepository.findByClinicIdAndOverrideDate(clinicId, date);

            if (override.isPresent()) {

                SlotOverride exception = override.get();

                // Clinic closed on this date
                if (exception.isClosed()) {
                    continue;
                }

                // Generate slots using override
                generate(
                        slots,
                        date,
                        exception.getStartTime(),
                        exception.getEndTime());

            } else {

                // 2. No override: use weekly template
                SlotTemplate template = templateRepository
                        .findByClinicIdAndDayOfWeekAndActiveTrue(clinicId, date.getDayOfWeek())
                        .orElse(null);

                if (template != null)
                    generate(
                            slots,
                            date,
                            template.getStartTime(),
                            template.getEndTime());

            }
        }

        return slots;
    }

    private void generate(
            List<AppointmentSlotResponse> slots,
            LocalDate date,
            String start,
            String end) {

        LocalDateTime current = LocalDateTime.of(date, LocalTime.parse(start));

        LocalDateTime closing = LocalDateTime.of(date, LocalTime.parse(end));

        while (!current.plusMinutes(30).isAfter(closing)) {

            LocalDateTime slotEnd = current.plusMinutes(30);

            slots.add(AppointmentSlotResponse.fromDateTime(current));

            current = slotEnd;
        }
    }
}