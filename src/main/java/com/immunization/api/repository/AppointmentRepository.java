package com.immunization.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import com.immunization.api.entity.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    int countByClinicIdAndStartTime(Long clinicId, java.time.LocalDateTime startTime);
    Optional<Appointment> findByBookingReferenceAndPatientDateOfBirth(java.util.UUID bookingReference, java.time.LocalDate dateOfBirth);
}
