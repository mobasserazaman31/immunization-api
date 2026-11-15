package com.immunization.api.service;

import java.time.Instant;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.immunization.api.entity.TaskHistory;
import com.immunization.api.repository.TaskHistoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OneTimeInitializer {

    private static final String INITIAL_SLOT_SEED = "INITIAL_SLOT_SEED";

    private final TaskHistoryRepository taskHistoryRepository;
    private final SlotGenerationService slotGenerationService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void runOnceEver() {
        if (taskHistoryRepository.existsByTaskName(INITIAL_SLOT_SEED)) {
            return;
        }

        slotGenerationService.populateRollingWindow();
        taskHistoryRepository.save(new TaskHistory(INITIAL_SLOT_SEED, Instant.now()));
    }
}
