package com.immunization.api.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import com.immunization.api.entity.SlotTemplatePriority;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SlotTemplateRequest(
        @NotNull Long clinicId,
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotNull LocalDate validFrom,
        @NotNull LocalDate validTill,
        @Positive int capacity,
        SlotTemplatePriority priority) {
}
