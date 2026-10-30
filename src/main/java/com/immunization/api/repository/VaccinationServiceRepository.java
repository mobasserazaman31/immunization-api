package com.immunization.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.immunization.api.entity.VaccinationService;

@Repository
public interface VaccinationServiceRepository extends JpaRepository<VaccinationService, Long> {
}
