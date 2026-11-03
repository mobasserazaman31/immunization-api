package com.immunization.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.immunization.api.repository.ClinicRepository;
import com.immunization.api.entity.Clinic;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;


@RestController 
@RequestMapping ("/api/clinics")
public class ClinicController {

    private final ClinicRepository clinicRepository;

    ClinicController(ClinicRepository clinicRepository){
        this.clinicRepository = clinicRepository;
    }

    @GetMapping
    public List<Clinic> getClinics() {
        return clinicRepository.findAll();
    }
    
    
}
