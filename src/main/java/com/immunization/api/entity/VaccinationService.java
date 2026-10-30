package com.immunization.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity 
@Table (name = "vaccination_services")
@Getter 
public class VaccinationService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(nullable = false)
    private boolean active = true;

    VaccinationService(String name, String description, int durationMinutes, boolean active) {
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.active = active;
    }
}