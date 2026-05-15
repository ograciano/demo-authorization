package com.vass.authorization.dto;

import java.time.Instant;
import java.util.List;

public record AdminReplacePermissionsResponseDTO(
        Long userId,
        List<String> permissions,
        String status,
        Instant timestamp
) {
}
