package com.immunization.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.immunization.api.dto.VaccinationServiceRequest;
import com.immunization.api.entity.VaccinationService;
import com.immunization.api.repository.VaccinationServiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VaccinationServiceService {

    private final VaccinationServiceRepository vaccinationServiceRepository;

    public List<VaccinationService> getAll() {
        return vaccinationServiceRepository.findAll();
    }

    public VaccinationService create(VaccinationServiceRequest request) {
        VaccinationService vaccinationService = new VaccinationService(
                request.name(),
                request.description());
        return vaccinationServiceRepository.save(vaccinationService);
    }
}
