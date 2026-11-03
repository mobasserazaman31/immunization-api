package com.immunization.api.controller;

import com.immunization.api.entity.VaccinationService;
import com.immunization.api.repository.VaccinationServiceRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;


@RestController 
@RequestMapping ("/api/vaccination-services")
public class VaccinationServiceController {

    private final VaccinationServiceRepository vaccinationServiceRepository;

    VaccinationServiceController(VaccinationServiceRepository vaccinationServiceRepository) {
        this.vaccinationServiceRepository = vaccinationServiceRepository;
    }

    @GetMapping
    public List<VaccinationService> getVaccinationServices() {
        return vaccinationServiceRepository.findAll();
    }
    


    
}
