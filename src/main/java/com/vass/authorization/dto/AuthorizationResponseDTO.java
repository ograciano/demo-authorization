package com.vass.authorization.dto;

public record AuthorizationResponseDTO(
        boolean allowed,
        Long userId,
        String resource,
        String action,
        String reason
) {
}
