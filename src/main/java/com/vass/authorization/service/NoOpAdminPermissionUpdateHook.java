package com.vass.authorization.service;

import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class NoOpAdminPermissionUpdateHook implements AdminPermissionUpdateHook {

    @Override
    public void beforeCommit(Long userId, Set<String> previousPermissions, Set<String> updatedPermissions) {
        // No-op hook used in production flow.
    }
}
