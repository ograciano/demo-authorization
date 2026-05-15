package com.vass.authorization.dto;

import java.time.Instant;
import java.util.List;

public record UserPermissionsResponseDTO(
        Long userId,
        List<String> permissions,
        Instant timestamp
) {
}
