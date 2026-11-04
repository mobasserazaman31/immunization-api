package com.immunization.api.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity 
@Table (name = "appointment_slots")
@Getter 
@NoArgsConstructor 
public class AppointmentSlot {

    public static final int DEFAULT_DURATION_MINUTES = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "clinic_id", nullable = false)
    private Clinic clinic;

    //maps to TIMESTAMPTZ (with timezone)
    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(nullable = false)
    private boolean active = true;

    public OffsetDateTime getEndTime() {
        return startTime.plusMinutes(DEFAULT_DURATION_MINUTES);
    }

    AppointmentSlot(Clinic clinic, OffsetDateTime startTime, boolean active) {
        this.clinic = clinic;
        this.startTime = startTime;
        this.active = active;
    }
}
