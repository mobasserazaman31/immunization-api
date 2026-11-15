package com.immunization.api.repository;


import com.immunization.api.entity.SlotOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SlotOverrideRepository
        extends JpaRepository<SlotOverride, Long> {

    Optional<SlotOverride> findByClinicIdAndOverrideDate(
        Long clinicId,
        LocalDate overrideDate
    );

    List<SlotOverride> findByClinicIdAndActiveTrueAndOverrideDateBetween(
        Long clinicId,
        LocalDate startDate,
        LocalDate endDate
    );
}