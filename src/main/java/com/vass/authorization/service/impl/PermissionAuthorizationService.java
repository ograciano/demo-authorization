package com.vass.authorization.service.impl;

import com.vass.authorization.api.dto.request.PermissionCheckRequest;
import com.vass.authorization.entity.UserEntity;
import com.vass.authorization.repository.UserRepository;
import com.vass.authorization.service.AuditService;
import com.vass.authorization.service.AuthorizationService;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionAuthorizationService implements AuthorizationService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    public PermissionAuthorizationService(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkPermission(PermissionCheckRequest request) {
        Optional<UserEntity> userOptional = userRepository.findById(request.userId());
        if (userOptional.isEmpty()) {
            registerDenied(request);
            return false;
        }

        UserEntity userEntity = userOptional.get();
        if (!userEntity.isActive()) {
            registerDenied(request);
            return false;
        }

        Set<String> effectivePermissions = new HashSet<>(userRepository.findActivePermissionCodesByUserId(request.userId()));
        effectivePermissions.addAll(userRepository.findDirectPermissionCodesByUserId(request.userId()));
        String requiredPermission = toPermissionCode(request.resource(), request.action());
        boolean allowed = effectivePermissions.contains(requiredPermission);
        if (!allowed) {
            registerDenied(request);
        }
        return allowed;
    }

    private String toPermissionCode(String resource, String action) {
        return resource.trim().toUpperCase(Locale.ROOT) + ":" + action.trim().toUpperCase(Locale.ROOT);
    }

    private void registerDenied(PermissionCheckRequest request) {
        auditService.registerDeniedPermissionCheck(request.userId(), request.resource(), request.action());
    }
}
