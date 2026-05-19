package com.vass.authorization.service;

import com.vass.authorization.api.dto.response.AdminPermissionReplacementResponse;
import java.util.List;

public interface AdminPermissionReplacementService {

    AdminPermissionReplacementResponse replacePermissions(Long userId, List<String> permissions, String actor);
}
