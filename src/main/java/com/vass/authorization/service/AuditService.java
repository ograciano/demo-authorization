package com.vass.authorization.service;

import java.util.Set;

public interface AuditService {

    void registerDeniedPermissionCheck(Long userId, String resource, String action);

    void registerAdminPermissionReplacement(
        String actor,
        Long targetUserId,
        Set<String> beforePermissions,
        Set<String> afterPermissions
    );
}
