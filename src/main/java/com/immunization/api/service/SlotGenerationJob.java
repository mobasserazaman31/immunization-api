package com.immunization.api.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SlotGenerationJob {

    private final SlotGenerationService slotGenerationService;
    private final SlotRetentionService slotRetentionService;

    @Scheduled(cron = "0 0 2 * * *")
    public void generateDaily() {
        slotGenerationService.generateNewRollingDay();
    }

    @Scheduled(cron = "0 15 2 * * *")
    public void deleteExpiredSlotsAndAppointments() {
        slotRetentionService.deleteExpiredSlotsAndAppointments();
    }
}
