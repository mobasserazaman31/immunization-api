package com.immunization.api.repository;

import java.util.List;

import com.immunization.api.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {

	List<Clinic> findByActiveTrue();
}
