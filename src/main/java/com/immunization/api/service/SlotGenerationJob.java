package com.immunization.api.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SlotGenerationJob {

    private final SlotGenerationService slotGenerationService;
    private final SlotRetentionService slotRetentionService;

    @EventListener(ApplicationReadyEvent.class)
    public void populateInitialWindow() {
        slotGenerationService.populateRollingWindow();
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void generateDaily() {
        slotGenerationService.generateNewRollingDay();
    }

    @Scheduled(cron = "0 15 2 * * *")
    public void deleteExpiredSlotsAndAppointments() {
        slotRetentionService.deleteExpiredSlotsAndAppointments();
    }
}
