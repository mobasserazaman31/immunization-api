package com.immunization.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClinicRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 250) String address,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String province) {
}
