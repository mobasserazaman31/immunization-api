package com.immunization.api.service;

import com.immunization.api.dto.AppointmentSlotResponse;
import com.immunization.api.entity.*;
import com.immunization.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotTemplateRepository templateRepository;
    private final SlotOverrideRepository overrideRepository;
    private final AppointmentRepository appointmentRepository;
    private final Logger logger = LoggerFactory.getLogger(SlotService.class);

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
                        clinicId,
                        slots,
                        date,
                        exception.getStartTime(),
                        exception.getEndTime(),
                        exception.getCapacity());

            } else {

                // 2. No override: use weekly template
                SlotTemplate template = templateRepository
                        .findByClinicIdAndDayOfWeekAndActiveTrue(clinicId, date.getDayOfWeek())
                        .orElse(null);

                logger.info("Template for clinicId: {}, date: {}, template: {}", clinicId, date, template != null ? "{" + template.getDayOfWeek() + "," + template.getStartTime() + "," + template.getEndTime() + "}" : "None");

                
                if (template != null)
                    generate(
                            clinicId,
                            slots,
                            date,
                            template.getStartTime(),
                            template.getEndTime(),
                            template.getCapacity());

            }
        }

        return slots;
    }

    private void generate(
            Long clinicId,
            List<AppointmentSlotResponse> slots,
            LocalDate date,
            String start,
            String end,
            Integer capacity) {

        LocalDateTime current = LocalDateTime.of(date, LocalTime.parse(start));

        LocalDateTime closing = LocalDateTime.of(date, LocalTime.parse(end));

        logger.info("Generating slots for clinicId: {}, date: {}, openingAt: {}, closingAt: {}, capacity: {}", clinicId, date, current, closing, capacity);


        while (!current.plusMinutes(30).isAfter(closing)) {

            LocalDateTime slotEnd = current.plusMinutes(30);

            int bookedCount = appointmentRepository.countByClinicIdAndStartTime(clinicId, current);
            logger.info("Slot: {} - {}, Booked: {}, Capacity: {}", current, slotEnd, bookedCount, capacity);

            if(bookedCount < capacity) {
                slots.add(AppointmentSlotResponse.fromDateTime(current));
            }

            current = slotEnd;
        }
    }

    public List<SlotTemplate> getSlotTemplates(Long clinicId) {
        return templateRepository.findByClinicIdAndActiveTrue(clinicId);
    }
}