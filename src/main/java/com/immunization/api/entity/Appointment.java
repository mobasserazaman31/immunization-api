package com.immunization.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.Getter;

@Entity
@Table(name = "appointments")
@NoArgsConstructor 
@Getter
@Setter 
public class Appointment {

    public static final int DEFAULT_DURATION_MINUTES = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", nullable = false, unique = true)
    private UUID bookingReference;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private VaccinationService service;

    @Column(name = "patient_first_name", nullable = false, length = 100)
    private String patientFirstName;

    @Column(name = "patient_last_name", nullable = false, length = 100)
    private String patientLastName;

    @Column(name = "patient_date_of_birth", nullable = false)
    private LocalDate patientDateOfBirth;

    @Column(name = "guardian_name", nullable = false)
    private String guardianName;

    @Column(name = "guardian_email", nullable = false)
    private String guardianEmail;

    @Column(name = "guardian_phone")
    private String guardianPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.BOOKED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public LocalDateTime getEndTime() {
        return getStartTime().plusMinutes(DEFAULT_DURATION_MINUTES);
    }

    public Clinic getClinic() {
        return slot == null ? null : slot.getClinic();
    }

    public LocalDateTime getStartTime() {
        return slot == null ? null : slot.getStartTime();
    }

    public Appointment(
            Slot slot,
            VaccinationService service,
            String patientFirstName,
            String patientLastName,
            LocalDate patientDateOfBirth,
            String guardianName,
            String guardianEmail,
            String guardianPhone,
            AppointmentStatus status
    ) {
        this.slot = slot;
        this.service = service;
        this.patientFirstName = patientFirstName;
        this.patientLastName = patientLastName;
        this.patientDateOfBirth = patientDateOfBirth;
        this.guardianName = guardianName;
        this.guardianEmail = guardianEmail;
        this.guardianPhone = guardianPhone;
        this.status = status != null ? status : AppointmentStatus.BOOKED;
        this.bookingReference = UUID.randomUUID();
        this.createdAt = OffsetDateTime.now();
    }
}

