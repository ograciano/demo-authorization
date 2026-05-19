package com.vass.authorization.service.impl;

import com.vass.authorization.api.dto.response.AdminPermissionReplacementResponse;
import com.vass.authorization.entity.PermissionEntity;
import com.vass.authorization.entity.UserEntity;
import com.vass.authorization.repository.PermissionRepository;
import com.vass.authorization.repository.UserRepository;
import com.vass.authorization.service.AdminPermissionReplacementService;
import com.vass.authorization.service.AuditService;
import com.vass.authorization.service.exception.InvalidPermissionException;
import com.vass.authorization.service.exception.UserNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminPermissionReplacementServiceImpl implements AdminPermissionReplacementService {

    private static final String UPDATED = "UPDATED";

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final AuditService auditService;
    private final Set<String> allowedPermissions;

    public AdminPermissionReplacementServiceImpl(
        UserRepository userRepository,
        PermissionRepository permissionRepository,
        AuditService auditService,
        @Value("${app.permissions.allowed}") String allowedPermissions
    ) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.auditService = auditService;
        this.allowedPermissions = Arrays.stream(allowedPermissions.split(","))
            .map(String::trim)
            .filter(permission -> !permission.isBlank())
            .map(permission -> permission.toUpperCase(Locale.ROOT))
            .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public AdminPermissionReplacementResponse replacePermissions(Long userId, List<String> permissions, String actor) {
        Set<String> normalizedPermissions = normalizePermissions(permissions);
        validateAllowedPermissions(normalizedPermissions);

        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Set<String> beforePermissions = userEntity.getDirectPermissions()
            .stream()
            .map(PermissionEntity::getCode)
            .collect(Collectors.toCollection(TreeSet::new));

        userEntity.getDirectPermissions().clear();
        for (String permission : normalizedPermissions) {
            PermissionEntity permissionEntity = permissionRepository.findByCode(permission)
                .orElseGet(() -> permissionRepository.save(new PermissionEntity(permission)));
            userEntity.addDirectPermission(permissionEntity);
        }
        userRepository.save(userEntity);

        Set<String> afterPermissions = userEntity.getDirectPermissions()
            .stream()
            .map(PermissionEntity::getCode)
            .collect(Collectors.toCollection(TreeSet::new));

        auditService.registerAdminPermissionReplacement(actor, userId, beforePermissions, afterPermissions);

        return new AdminPermissionReplacementResponse(userId, new ArrayList<>(afterPermissions), UPDATED);
    }

    private Set<String> normalizePermissions(List<String> permissions) {
        if (permissions == null) {
            throw new InvalidPermissionException("null");
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String permission : permissions) {
            if (permission == null || permission.isBlank()) {
                throw new InvalidPermissionException(String.valueOf(permission));
            }
            normalized.add(permission.trim().toUpperCase(Locale.ROOT));
        }
        return normalized;
    }

    private void validateAllowedPermissions(Set<String> permissions) {
        for (String permission : permissions) {
            if (!allowedPermissions.contains(permission)) {
                throw new InvalidPermissionException(permission);
            }
        }
    }
}
