package com.vass.authorization.dto;

import java.time.Instant;

public record AssignPermissionResponseDTO(
        Long userId,
        String permission,
        String status,
        Instant timestamp
) {
}
