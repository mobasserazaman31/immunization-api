package com.immunization.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.immunization.api.dto.ClinicRequest;
import com.immunization.api.entity.Clinic;
import com.immunization.api.repository.ClinicRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClinicService {

    private final ClinicRepository clinicRepository;

    public List<Clinic> getAll() {
        return clinicRepository.findAll();
    }

    public Clinic create(ClinicRequest request) {
        Clinic clinic = new Clinic(
                request.name(),
                request.address(),
                request.city(),
                request.province());
        return clinicRepository.save(clinic);
    }
}
