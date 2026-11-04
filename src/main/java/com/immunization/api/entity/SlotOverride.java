package com.immunization.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "slot_overrides",
    uniqueConstraints = @UniqueConstraint(columnNames = {"clinic_id", "override_date"})
)
@Getter 
@Setter
@NoArgsConstructor
public class SlotOverride {

    public static final int DEFAULT_DURATION_MINUTES = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Column (name = "override_date", nullable = false)
    private LocalDate overrideDate;

    @Column (name = "closed", nullable = false)
    private boolean closed;

    @Column (name = "start_time", nullable = false)
    private String startTime;

    @Column  (name = "reason", nullable = false)
    private String reason;

    @Column (name = "capacity", nullable = false)
    private int capacity;

    public String getEndTime() {
        return LocalTime.parse(startTime).plusMinutes(DEFAULT_DURATION_MINUTES).toString();
    }
} 