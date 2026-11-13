package com.immunization.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.immunization.api.entity.Slot;
import com.immunization.api.entity.SlotStatus;

import jakarta.persistence.LockModeType;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByClinic_IdAndStartTimeGreaterThanEqualAndStartTimeLessThanAndStatusAndActiveTrueOrderByStartTimeAsc(
            Long clinicId,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive,
            SlotStatus status);

    boolean existsByClinic_IdAndStartTimeGreaterThanEqualAndStartTimeLessThan(
            Long clinicId,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select slot from Slot slot where slot.clinic.id = :clinicId "
            + "and slot.startTime >= :startInclusive and slot.startTime < :endExclusive")
    List<Slot> findForUpdateByClinicAndStartTimeRange(
            @Param("clinicId") Long clinicId,
            @Param("startInclusive") LocalDateTime startInclusive,
            @Param("endExclusive") LocalDateTime endExclusive);

    @Modifying
    @Query(value = """
            INSERT INTO slots (clinic_id, start_time, end_time, capacity, booked_count, status, active)
            VALUES (:clinicId, :startTime, :endTime, :capacity, 0, 'AVAILABLE', TRUE)
            ON CONFLICT (clinic_id, start_time) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("clinicId") Long clinicId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("capacity") int capacity);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select slot from Slot slot where slot.clinic.id = :clinicId and slot.startTime = :startTime")
    Optional<Slot> findForUpdate(
            @Param("clinicId") Long clinicId,
            @Param("startTime") LocalDateTime startTime);

    @Modifying
    @Query("delete from Slot slot where slot.startTime < :cutoff")
    int deleteStartingBefore(@Param("cutoff") LocalDateTime cutoff);
}
