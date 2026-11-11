package com.immunization.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.immunization.api.entity.VaccinationService;
import com.immunization.api.repository.VaccinationServiceRepository;

@RestController
@RequestMapping("/api/vaccination-services")
public class VaccinationServiceController {

    private final VaccinationServiceRepository vaccinationServiceRepository;

    public VaccinationServiceController(VaccinationServiceRepository vaccinationServiceRepository) {
        this.vaccinationServiceRepository = vaccinationServiceRepository;
    }

    @GetMapping
    public List<VaccinationService> getVaccinationServices() {
        return vaccinationServiceRepository.findAll();
    }
}
