package com.immunization.api.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.immunization.api.repository.AppointmentRepository;
import com.immunization.api.repository.SlotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlotRetentionService {

    private static final Logger logger = LoggerFactory.getLogger(SlotRetentionService.class);

        private final AppointmentRepository appointmentRepository;
        private final SlotRepository slotRepository;

    @Transactional
    public void deleteExpiredSlotsAndAppointments() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(1);

        int deletedAppointments = appointmentRepository.deleteForSlotsStartingBefore(cutoff);
        int deletedSlots = slotRepository.deleteStartingBefore(cutoff);

        logger.info("Deleted {} appointments and {} slots older than {}",
                deletedAppointments,
                deletedSlots,
                cutoff);
    }
}
