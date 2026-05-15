package com.vass.authorization.service;

import java.util.Set;

public interface AdminPermissionUpdateHook {

    void beforeCommit(Long userId, Set<String> previousPermissions, Set<String> updatedPermissions);
}
