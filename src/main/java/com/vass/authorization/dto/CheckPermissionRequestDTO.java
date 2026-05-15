package com.vass.authorization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CheckPermissionRequestDTO(
        @NotNull @Positive Long userId,
        @NotBlank String resource,
        @NotBlank String action
) {
}
