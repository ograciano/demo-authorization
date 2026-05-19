package com.vass.authorization.api.dto.response;

public record PermissionAssignmentResponse(
    Long userId,
    String permission,
    String status
) {
}
