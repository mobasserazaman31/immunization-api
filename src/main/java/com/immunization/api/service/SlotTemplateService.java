package com.immunization.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.immunization.api.dto.SlotTemplateRequest;
import com.immunization.api.entity.Clinic;
import com.immunization.api.entity.SlotTemplate;
import com.immunization.api.entity.SlotTemplatePriority;
import com.immunization.api.repository.ClinicRepository;
import com.immunization.api.repository.SlotTemplateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlotTemplateService {

    private static final LocalTime SLOT_GENERATION_TIME = LocalTime.of(2, 0);

    private final ClinicRepository clinicRepository;
    private final SlotTemplateRepository templateRepository;

    @Transactional
    public SlotTemplate create(SlotTemplateRequest request) {
        Clinic clinic = clinicRepository.findById(request.clinicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));

        if (!request.endTime().isAfter(request.startTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime must be after startTime");
        }
        if (request.validTill().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validTill must be today or a future date");
        }
        if (request.validTill().isBefore(request.validFrom())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validTill must not be before validFrom");
        }
        LocalDate firstApplicableDate = request.validFrom()
            .with(TemporalAdjusters.nextOrSame(request.dayOfWeek()));
        if (firstApplicableDate.isAfter(request.validTill())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "The validity period must include at least one " + request.dayOfWeek() + "");
        }
        LocalDate generationDate = firstApplicableDate.minusDays(SlotGenerationService.WINDOW_DAYS - 1);
        LocalDateTime generationDeadline = generationDate.atTime(SLOT_GENERATION_TIME);
        if (!LocalDateTime.now().isBefore(generationDeadline)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "The first " + request.dayOfWeek() + " date (" + firstApplicableDate
                    + ") is generated at 02:00 on " + generationDate
                    + ". Submit at least 6 days ahead, before that generation run."
                    + " Choose a later validFrom date and resubmit, or create a date-specific override"
                    + " to correct an already generated date in an emergency.");
        }
        SlotTemplatePriority priority = request.priority() == null
                ? SlotTemplatePriority.DEFAULT
                : request.priority();
        if (templateRepository
                .existsByClinic_IdAndDayOfWeekAndPriorityAndValidFromLessThanEqualAndValidTillGreaterThanEqual(
                        clinic.getId(), request.dayOfWeek(), priority, request.validTill(), request.validFrom())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                        "A template with the same priority already overlaps this clinic's weekday and validity period."
                            + " Check the existing template and resolve the conflict manually; if needed, delete it"
                            + " before creating the new template.");
        }
        SlotTemplate template = new SlotTemplate();
        template.setClinic(clinic);
        template.setDayOfWeek(request.dayOfWeek());
        template.setStartTime(request.startTime().toString());
        template.setEndTime(request.endTime().toString());
        template.setValidFrom(request.validFrom());
        template.setValidTill(request.validTill());
        template.setCapacity(request.capacity());
        template.setPriority(priority);

        SlotTemplate savedTemplate = templateRepository.saveAndFlush(template);
        return savedTemplate;
    }

    public List<SlotTemplate> getActiveForClinic(Long clinicId) {
        return templateRepository.findByClinicIdAndActiveTrue(clinicId);
    }
}
