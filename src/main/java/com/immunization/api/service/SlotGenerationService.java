package com.immunization.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.immunization.api.entity.Appointment;
import com.immunization.api.entity.AppointmentStatus;
import com.immunization.api.entity.Clinic;
import com.immunization.api.entity.Slot;
import com.immunization.api.entity.SlotOverride;
import com.immunization.api.entity.SlotStatus;
import com.immunization.api.entity.SlotTemplate;
import com.immunization.api.repository.AppointmentRepository;
import com.immunization.api.repository.ClinicRepository;
import com.immunization.api.repository.SlotRepository;
import com.immunization.api.repository.SlotOverrideRepository;
import com.immunization.api.repository.SlotTemplateRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlotGenerationService {

    public static final int WINDOW_DAYS = 7;
    private static final int SLOT_DURATION_MINUTES = 30;
    private static final Logger logger = LoggerFactory.getLogger(SlotGenerationService.class);

    private final ClinicRepository clinicRepository;
    private final SlotTemplateRepository templateRepository;
    private final SlotOverrideRepository overrideRepository;
    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public void populateRollingWindow() {
        LocalDate firstDate = LocalDate.now();
        LocalDate lastDate = firstDate.plusDays(WINDOW_DAYS - 1);
        for (Clinic clinic : clinicRepository.findByActiveTrue()) {
            generateClinicDates(clinic, firstDate, lastDate, false);
        }
    }

    @Transactional
    public void generateNewRollingDay() {
        LocalDate newWindowDate = LocalDate.now().plusDays(WINDOW_DAYS - 1);
        for (Clinic clinic : clinicRepository.findByActiveTrue()) {
            generateClinicDates(clinic, newWindowDate, newWindowDate, false);
        }
    }

    @Transactional
    public void regenerateDate(Long clinicId, LocalDate date) {
        clinicRepository.findById(clinicId)
                .ifPresent(clinic -> generateClinicDates(clinic, date, date, true));
    }

    private void generateClinicDates(
            Clinic clinic,
            LocalDate firstDate,
            LocalDate lastDate,
            boolean updateExisting) {
        Long clinicId = clinic.getId();
        List<SlotTemplate> templates = loadActiveTemplates(clinicId);
        Map<LocalDate, SlotOverride> overrides = loadOverrides(clinicId, firstDate, lastDate);

        for (LocalDate date = firstDate; !date.isAfter(lastDate); date = date.plusDays(1)) {
            SlotOverride override = overrides.get(date);
            SlotTemplate template = resolveEffectiveTemplate(date, templates);
            Map<LocalDateTime, Integer> desiredSlots = buildDaySchedule(date, override, template);

            if (updateExisting) {
                syncExistingDaySlots(clinicId, date, desiredSlots, override);
            }

            insertNewDaySlots(clinicId, desiredSlots);
        }
    }

    private List<SlotTemplate> loadActiveTemplates(Long clinicId) {
        return templateRepository.findByClinicIdAndActiveTrue(clinicId);
    }

    private Map<LocalDate, SlotOverride> loadOverrides(Long clinicId, LocalDate firstDate, LocalDate lastDate) {
        return overrideRepository.findByClinicIdAndActiveTrueAndOverrideDateBetween(clinicId, firstDate, lastDate)
                .stream()
                .collect(Collectors.toMap(SlotOverride::getOverrideDate, Function.identity()));
    }

    private SlotTemplate resolveEffectiveTemplate(LocalDate date, List<SlotTemplate> templates) {
        return templates.stream()
                .filter(template -> template.getDayOfWeek() == date.getDayOfWeek())
                .filter(template -> isValidOn(template, date))
                .sorted(Comparator
                        .comparingInt((SlotTemplate template) -> template.getPriority().getPrecedence())
                        .reversed())
                .findFirst()
                .orElse(null);
    }

    private Map<LocalDateTime, Integer> buildDaySchedule(
            LocalDate date,
            SlotOverride override,
            SlotTemplate template) {
        boolean closed = override != null && override.isClosed();
        String start = null;
        String end = null;

        if (!closed) {
            if (override != null) {
                start = override.getStartTime();
                end = override.getEndTime();
            } else if (template != null) {
                start = template.getStartTime();
                end = template.getEndTime();
            }
        }

        Map<LocalDateTime, Integer> desiredSlots = new HashMap<>();
        if (start == null || end == null) {
            return desiredSlots;
        }

        int capacity = override != null ? override.getCapacity() : template.getCapacity();
        LocalTime openingTime = LocalTime.parse(start);
        LocalTime closingTime = LocalTime.parse(end);

        for (LocalTime time = openingTime; !time.plusMinutes(SLOT_DURATION_MINUTES).isAfter(closingTime); time = time
                .plusMinutes(SLOT_DURATION_MINUTES)) {
            desiredSlots.put(LocalDateTime.of(date, time), capacity);
        }

        return desiredSlots;
    }

    private void syncExistingDaySlots(
            Long clinicId,
            LocalDate date,
            Map<LocalDateTime, Integer> desiredSlots,
            SlotOverride override) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        for (Slot slot : slotRepository.findForUpdateByClinicAndStartTimeRange(clinicId, dayStart, dayEnd)) {
            Integer capacity = desiredSlots.remove(slot.getStartTime());
            if (capacity == null) {
                cancelInvalidBookings(slot, 0, date, override);
                slot.setActive(false);
                slot.setBookedCount(0);
                slot.setStatus(SlotStatus.AVAILABLE);
            } else {
                int bookedCount = cancelInvalidBookings(slot, capacity, date, override);
                slot.setEndTime(slot.getStartTime().plusMinutes(SLOT_DURATION_MINUTES));
                slot.setCapacity(capacity);
                slot.setBookedCount(bookedCount);
                slot.setStatus(bookedCount == capacity ? SlotStatus.BOOKED : SlotStatus.AVAILABLE);
                slot.setActive(true);
            }
        }
    }

    private void insertNewDaySlots(Long clinicId, Map<LocalDateTime, Integer> desiredSlots) {
        desiredSlots.forEach((slotStart, capacity) -> slotRepository.insertIfAbsent(
                clinicId,
                slotStart,
                slotStart.plusMinutes(SLOT_DURATION_MINUTES),
                capacity));
    }

    private boolean isValidOn(SlotTemplate template, LocalDate date) {
        return (template.getValidFrom() == null || !date.isBefore(template.getValidFrom()))
                && (template.getValidTill() == null || !date.isAfter(template.getValidTill()));
    }

    private int cancelInvalidBookings(Slot slot, int maximumBookings, LocalDate date, SlotOverride override) {
        var bookedAppointments = appointmentRepository.findBySlotIdAndStatusForUpdate(
                slot.getId(),
                AppointmentStatus.BOOKED);
        int cancellations = Math.max(0, bookedAppointments.size() - maximumBookings);
        String reason = override == null ? "the updated clinic schedule" : override.getReason();

        for (int index = 0; index < cancellations; index++) {
            Appointment appointment = bookedAppointments.get(index);
            appointment.setStatus(AppointmentStatus.CANCELLED);
            logger.warn(
                    "Cancelled appointment bookingReference={} clinicId={} slotStart={} overrideDate={} reason={}",
                    appointment.getBookingReference(),
                    slot.getClinic().getId(),
                    slot.getStartTime(),
                    date,
                    reason);
        }

        return bookedAppointments.size() - cancellations;
    }
}
