package com.vass.authorization.service;

import com.vass.authorization.api.dto.response.PermissionAssignmentResponse;

public interface UserPermissionAssignmentService {

    PermissionAssignmentResponse assignPermission(Long userId, String permission);
}
