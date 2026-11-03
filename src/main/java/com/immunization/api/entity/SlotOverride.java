package com.immunization.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "slot_overrides",
    uniqueConstraints = @UniqueConstraint(columnNames = {"clinic_id", "override_date"})
)
@Getter 
@Setter
@NoArgsConstructor
public class SlotOverride {

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

    @Column (name = "end_time", nullable = false)
    private String endTime;

    // @Column  (name = "reason")
    // private String reason;
} 