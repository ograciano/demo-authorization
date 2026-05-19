package com.vass.authorization.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AdminPermissionReplacementRequest(
    @NotNull(message = "permissions is required")
    List<@NotBlank(message = "permissions must not contain blank values") String> permissions
) {
}
