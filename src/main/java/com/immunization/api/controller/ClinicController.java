package com.immunization.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.immunization.api.entity.Clinic;
import com.immunization.api.repository.ClinicRepository;

@RestController
@RequestMapping("/api/clinics")
public class ClinicController {

    private final ClinicRepository clinicRepository;

    public ClinicController(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }

    @GetMapping
    public List<Clinic> getClinics() {
        return clinicRepository.findAll();
    }
}
