package com.immunization.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.immunization.api.dto.AppointmentRequest;
import com.immunization.api.entity.Appointment;
import com.immunization.api.entity.AppointmentStatus;
import com.immunization.api.entity.Slot;
import com.immunization.api.entity.SlotStatus;
import com.immunization.api.entity.VaccinationService;
import com.immunization.api.repository.AppointmentRepository;
import com.immunization.api.repository.ClinicRepository;
import com.immunization.api.repository.SlotRepository;
import com.immunization.api.repository.VaccinationServiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final ClinicRepository clinicRepository;
    private final VaccinationServiceRepository vaccinationServiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;

    @Transactional
    public Appointment createAppointment(AppointmentRequest appointmentRequest) {
        Long clinicId = appointmentRequest.clinicId();
        if (clinicId == null || !clinicRepository.existsById(clinicId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found");
        }

        VaccinationService service = vaccinationServiceRepository.findById(appointmentRequest.serviceId())
                .orElseThrow(() -> new IllegalArgumentException("Vaccination service not found"));

        String patientOhipNumber = appointmentRequest.patientOhipNumber().toUpperCase(Locale.ROOT);
        appointmentRepository.lockForPatientBooking(patientOhipNumber);

        if (appointmentRepository.existsByPatientOhipNumberIgnoreCaseAndService_IdAndStatus(
                patientOhipNumber, service.getId(), AppointmentStatus.BOOKED)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This patient already has an active appointment for this service");
        }

        LocalDateTime requestedStart = appointmentRequest.startTime();
        Slot slot = slotRepository.findForUpdate(clinicId, requestedStart)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Requested slot is unavailable"));

        if (appointmentRepository
                .existsByPatientOhipNumberIgnoreCaseAndStatusAndSlot_StartTimeLessThanAndSlot_EndTimeGreaterThan(
                        patientOhipNumber,
                        AppointmentStatus.BOOKED,
                        slot.getEndTime(),
                        slot.getStartTime())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This patient already has an overlapping appointment");
        }

        if (!slot.isActive()
                || slot.getStatus() != SlotStatus.AVAILABLE
                || slot.getBookedCount() >= slot.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Requested slot is full or unavailable");
        }

        slot.setBookedCount(slot.getBookedCount() + 1);
        slot.setStatus(slot.getBookedCount() == slot.getCapacity()
                ? SlotStatus.BOOKED
                : SlotStatus.AVAILABLE);
        slotRepository.save(slot);

        Appointment appointment = new Appointment(
                slot,
                service,
                appointmentRequest.patientFirstName(),
                appointmentRequest.patientLastName(),
                appointmentRequest.patientDateOfBirth(),
                patientOhipNumber,
                appointmentRequest.guardianName(),
                appointmentRequest.guardianEmail(),
                appointmentRequest.guardianPhone(),
                AppointmentStatus.BOOKED);

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public boolean cancelAppointment(UUID bookingReference, LocalDate dateOfBirth) {

        Optional<Appointment> appointmentOpt = appointmentRepository
                .findByBookingReferenceAndPatientDateOfBirth(bookingReference, dateOfBirth);
        if (appointmentOpt.isPresent()) {
            Appointment appointmentReference = appointmentOpt.get();
            Slot slot = slotRepository.findForUpdate(
                    appointmentReference.getClinic().getId(),
                    appointmentReference.getStartTime())
                    .orElseThrow(() -> new IllegalStateException("Appointment slot not found"));
            Appointment appointment = appointmentRepository.findByIdForUpdate(appointmentReference.getId())
                    .orElseThrow(() -> new IllegalStateException("Appointment not found"));
            if (appointment.getStatus() != AppointmentStatus.BOOKED) {
                return true;
            }

            appointment.setStatus(AppointmentStatus.CANCELLED);
            slot.setBookedCount(Math.max(0, slot.getBookedCount() - 1));
            slot.setStatus(slot.getBookedCount() >= slot.getCapacity()
                    ? SlotStatus.BOOKED
                    : SlotStatus.AVAILABLE);
            slotRepository.save(slot);
            appointmentRepository.save(appointment);
            return true;
        }
        return false;

    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
}
