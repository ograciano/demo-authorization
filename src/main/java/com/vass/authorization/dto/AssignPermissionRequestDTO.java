package com.vass.authorization.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignPermissionRequestDTO(
        @NotBlank String permission
) {
}
