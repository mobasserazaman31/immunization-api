package com.immunization.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.immunization.api.dto.AppointmentRequest;
import com.immunization.api.entity.Appointment;
import com.immunization.api.entity.AppointmentStatus;
import com.immunization.api.entity.Clinic;
import com.immunization.api.entity.SlotOverride;
import com.immunization.api.entity.SlotTemplate;
import com.immunization.api.entity.VaccinationService;
import com.immunization.api.repository.AppointmentRepository;
import com.immunization.api.repository.ClinicRepository;
import com.immunization.api.repository.SlotOverrideRepository;
import com.immunization.api.repository.SlotTemplateRepository;
import com.immunization.api.repository.VaccinationServiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class AppointmentService {

    private final ClinicRepository clinicRepository;
    private final VaccinationServiceRepository vaccinationServiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final SlotTemplateRepository slotTemplateRepository;
    private final SlotOverrideRepository slotOverrideRepository;


    public Appointment createAppointment(AppointmentRequest appointmentRequest) {

        Clinic clinic = clinicRepository.findById(appointmentRequest.clinicId())
                .orElseThrow(() -> new IllegalArgumentException("Clinic not found"));

        VaccinationService service = vaccinationServiceRepository.findById(appointmentRequest.serviceId())
                .orElseThrow(() -> new IllegalArgumentException("Vaccination service not found"));

        LocalDateTime requestedStart = appointmentRequest.startTime();
        validateSlotAvailability(clinic.getId(), requestedStart);

        Appointment appointment = new Appointment(
                clinic,
                requestedStart,
                service,
                appointmentRequest.patientFirstName(),
                appointmentRequest.patientLastName(),
                appointmentRequest.patientDateOfBirth(),
                appointmentRequest.guardianName(),
                appointmentRequest.guardianEmail(),
                appointmentRequest.guardianPhone(),
                AppointmentStatus.BOOKED
        );

        return appointmentRepository.save(appointment);
    }

    private void validateSlotAvailability(Long clinicId, LocalDateTime requestedStart) {
        LocalDate requestedDate = requestedStart.toLocalDate();

        SlotOverride override = slotOverrideRepository
                .findByClinicIdAndOverrideDate(clinicId, requestedDate)
                .orElse(null);

        if (override != null && override.isClosed()) {
            throw new IllegalArgumentException("Clinic is closed on the requested date");
        }

        LocalTime requestedTime = requestedStart.toLocalTime();
        LocalTime slotStart;
        LocalTime slotEnd;
        int capacity;

        if (override != null) {
            slotStart = LocalTime.parse(override.getStartTime());
            slotEnd = LocalTime.parse(override.getEndTime());
            capacity = override.getCapacity();
        } else {
            SlotTemplate template = slotTemplateRepository
                    .findByClinicIdAndDayOfWeekAndActiveTrue(clinicId, requestedDate.getDayOfWeek())
                    .orElse(null);

            if (template == null) {
                throw new IllegalArgumentException("No time slot exists for this clinic and date");
            }

            slotStart = LocalTime.parse(template.getStartTime());
            slotEnd = LocalTime.parse(template.getEndTime());
            capacity = template.getCapacity();
        }

        if (!requestedTime.equals(slotStart) && !requestedTime.isAfter(slotStart) && !requestedTime.isBefore(slotStart)) {
            // no-op check to keep logic explicit while preserving the range validation below
        }

        boolean matchesSlot = false;
        LocalDateTime cursor = LocalDateTime.of(requestedDate, slotStart);
        LocalDateTime closing = LocalDateTime.of(requestedDate, slotEnd);

        while (!cursor.plusMinutes(30).isAfter(closing)) {
            if (cursor.equals(requestedStart)) {
                matchesSlot = true;
                break;
            }
            cursor = cursor.plusMinutes(30);
        }

        if (!matchesSlot) {
            throw new IllegalArgumentException("Requested time is not a valid slot for this clinic");
        }

        int bookedCount = appointmentRepository.countByClinicIdAndStartTime(clinicId, requestedStart);
        if (bookedCount >= capacity) {
            throw new IllegalStateException("Requested slot is full");
        }
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
}
