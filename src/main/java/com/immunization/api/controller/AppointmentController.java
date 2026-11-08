package com.immunization.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.immunization.api.dto.AppointmentRequest;
import com.immunization.api.entity.Appointment;
import com.immunization.api.service.AppointmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@RestController 
@RequestMapping ("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public Appointment createAppointment(@RequestBody AppointmentRequest appointmentRequest) {
        return appointmentService.createAppointment(appointmentRequest);

    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }
    

    @PostMapping("/cancel")
    public boolean cancelAppointment(@RequestParam UUID bookingReference, @RequestParam LocalDate dateOfBirth) {
        return appointmentService.cancelAppointment(bookingReference, dateOfBirth);
    }
    
    
    
}
