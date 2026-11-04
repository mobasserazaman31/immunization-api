package com.immunization.api.entity;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "slot_templates",  uniqueConstraints = @UniqueConstraint(columnNames = {"clinic_id", "day_of_week"}))
@Getter  
@Setter 
@NoArgsConstructor 
public class SlotTemplate {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Column (name = "day_of_week", nullable = false)
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Column (name = "start_time", nullable = false)
    private String startTime; 

    @Column (name = "end_time", nullable = false)
    private String endTime;

    @Column (name = "capacity", nullable = false)
    private int capacity;

    private boolean active = true;

}
