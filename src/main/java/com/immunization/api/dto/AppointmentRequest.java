package com.immunization.api.dto;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AppointmentRequest(
    Long clinicId, 
    Long serviceId, 
    LocalDateTime startTime, 
    LocalDate patientDateOfBirth, 
    String patientFirstName, 
    String patientLastName, 
    @NotBlank
    @Pattern(regexp = "[0-9]{10}([A-Za-z]{2})?", message = "OHIP number must be 10 digits with an optional 2-letter version code")
    String patientOhipNumber,
    String guardianName, 
    String guardianEmail, 
    String guardianPhone) {
    
}
