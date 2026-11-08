package com.immunization.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.immunization.api.dto.AppointmentSlotResponse;
import com.immunization.api.service.SlotService;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.immunization.api.entity.SlotTemplate;


@RestController 
@RequestMapping ("/api/appointment-slots")
public class AppointmentSlotController {

    private final SlotService appointmentSlotService;

    AppointmentSlotController(SlotService appointmentSlotService){
        this.appointmentSlotService = appointmentSlotService;
    }

    // @GetMapping
    // public List<AppointmentSlotResponse> getSlots(
    //         @RequestParam Long clinicId,

    //         @RequestParam
    //         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    //         LocalDate startDate,

    //         @RequestParam
    //         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    //         LocalDate endDate) {

    //     return appointmentSlotService.generateSlots(
    //         clinicId,
    //         startDate,
    //         endDate
    //     );
    // }
    

    @GetMapping
    public List<List<AppointmentSlotResponse>> getSlots(
            @RequestParam Long clinicId) {

        return appointmentSlotService.generateSlots(
            clinicId,
            LocalDate.now(),
            LocalDate.now().plusDays(7)
        );
    }

    @GetMapping("/templates")
    public List<SlotTemplate> getSlotTemplates(@RequestParam Long clinicId) {
        return appointmentSlotService.getSlotTemplates(clinicId);
    }
    
    
}
