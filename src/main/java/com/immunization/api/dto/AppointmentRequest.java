package com.immunization.api.dto;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AppointmentRequest(
    Long clinicId, 
    Long serviceId, 
    LocalDateTime startTime, 
    LocalDate patientDateOfBirth, 
    String patientFirstName, 
    String patientLastName, 
    String guardianName, 
    String guardianEmail, 
    String guardianPhone) {
    
}
