package com.immunization.api.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import com.immunization.api.entity.Appointment;
import com.immunization.api.entity.AppointmentStatus;
import jakarta.persistence.LockModeType;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

        Optional<Appointment> findByBookingReferenceAndPatientDateOfBirth(
            java.util.UUID bookingReference,
            LocalDate dateOfBirth);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select appointment from Appointment appointment where appointment.id = :id")
        Optional<Appointment> findByIdForUpdate(@Param("id") Long id);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select appointment from Appointment appointment "
            + "where appointment.slot.id = :slotId and appointment.status = :status "
            + "order by appointment.createdAt desc, appointment.id desc")
        List<Appointment> findBySlotIdAndStatusForUpdate(
            @Param("slotId") Long slotId,
            @Param("status") AppointmentStatus status);

    @Modifying
    @Query("delete from Appointment appointment where appointment.slot.id in "
            + "(select slot.id from Slot slot where slot.startTime < :cutoff)")
    int deleteForSlotsStartingBefore(@Param("cutoff") java.time.LocalDateTime cutoff);
}
