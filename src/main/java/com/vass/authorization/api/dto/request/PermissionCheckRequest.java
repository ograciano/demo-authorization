package com.vass.authorization.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PermissionCheckRequest(
    @NotNull(message = "userId is required")
    @Positive(message = "userId must be greater than 0")
    Long userId,
    @NotBlank(message = "resource is required")
    String resource,
    @NotBlank(message = "action is required")
    String action
) {
}
