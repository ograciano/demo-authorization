package com.vass.authorization.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AdminReplacePermissionsRequestDTO(
        @NotNull List<String> permissions
) {
}
