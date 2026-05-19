package com.vass.authorization.service;

import com.vass.authorization.api.dto.response.UserPermissionsResponse;

public interface UserPermissionsQueryService {

    UserPermissionsResponse getUserPermissions(Long userId);
}
