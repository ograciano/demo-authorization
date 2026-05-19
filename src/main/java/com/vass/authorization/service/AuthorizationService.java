package com.vass.authorization.service;

import com.vass.authorization.api.dto.request.PermissionCheckRequest;

public interface AuthorizationService {

    boolean checkPermission(PermissionCheckRequest request);
}
