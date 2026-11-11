package com.immunization.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import com.immunization.api.entity.Appointment;
import jakarta.persistence.LockModeType;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Appointment> findByBookingReferenceAndPatientDateOfBirth(java.util.UUID bookingReference, java.time.LocalDate dateOfBirth);

    @Modifying
    @Query("delete from Appointment appointment where appointment.slot.id in "
            + "(select slot.id from Slot slot where slot.startTime < :cutoff)")
    int deleteForSlotsStartingBefore(@Param("cutoff") java.time.LocalDateTime cutoff);
}
