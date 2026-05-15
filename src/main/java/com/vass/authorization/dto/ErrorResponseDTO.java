package com.vass.authorization.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponseDTO(
        String error,
        String message,
        Instant timestamp,
        Map<String, String> details
) {
}
