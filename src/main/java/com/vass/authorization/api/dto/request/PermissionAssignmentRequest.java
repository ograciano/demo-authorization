package com.vass.authorization.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PermissionAssignmentRequest(
    @NotBlank(message = "permission is required")
    String permission
) {
}
