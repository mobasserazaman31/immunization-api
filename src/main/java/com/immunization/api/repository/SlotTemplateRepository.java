package com.immunization.api.repository;

import com.immunization.api.entity.SlotTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.Optional;

public interface SlotTemplateRepository extends JpaRepository<SlotTemplate, Long> {

    Optional<SlotTemplate> findByClinicIdAndDayOfWeekAndActiveTrue(
        Long clinicId,
        DayOfWeek dayOfWeek
    );
}