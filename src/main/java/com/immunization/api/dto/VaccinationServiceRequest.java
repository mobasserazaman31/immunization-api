package com.immunization.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VaccinationServiceRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description) {
}
