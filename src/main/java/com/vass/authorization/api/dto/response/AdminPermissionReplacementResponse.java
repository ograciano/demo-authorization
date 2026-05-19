package com.vass.authorization.api.dto.response;

import java.util.List;

public record AdminPermissionReplacementResponse(
    Long userId,
    List<String> permissions,
    String status
) {
}
