package com.immunization.api.repository;

import com.immunization.api.entity.SlotTemplate;
import com.immunization.api.entity.SlotTemplatePriority;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface SlotTemplateRepository extends JpaRepository<SlotTemplate, Long> {

    boolean existsByClinic_IdAndDayOfWeek(Long clinicId, DayOfWeek dayOfWeek);

    boolean existsByClinic_IdAndDayOfWeekAndPriorityAndValidFromLessThanEqualAndValidTillGreaterThanEqual(
        Long clinicId,
        DayOfWeek dayOfWeek,
        SlotTemplatePriority priority,
        LocalDate validTill,
        LocalDate validFrom
    );

    Optional<SlotTemplate> findByClinicIdAndDayOfWeekAndActiveTrue(
        Long clinicId,
        DayOfWeek dayOfWeek
    );

    List<SlotTemplate> findByClinicIdAndActiveTrue(Long clinicId);
}