package com.vass.authorization.api.dto.response;

import java.time.Instant;
import java.util.List;

public record UserPermissionsResponse(
    Long userId,
    List<String> permissions,
    Instant timestamp
) {
}
