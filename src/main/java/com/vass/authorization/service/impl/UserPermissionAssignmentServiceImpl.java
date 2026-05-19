package com.vass.authorization.service.impl;

import com.vass.authorization.api.dto.response.PermissionAssignmentResponse;
import com.vass.authorization.entity.PermissionEntity;
import com.vass.authorization.entity.UserEntity;
import com.vass.authorization.repository.PermissionRepository;
import com.vass.authorization.repository.UserRepository;
import com.vass.authorization.service.UserPermissionAssignmentService;
import com.vass.authorization.service.exception.InvalidPermissionException;
import com.vass.authorization.service.exception.UserNotFoundException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPermissionAssignmentServiceImpl implements UserPermissionAssignmentService {

    private static final String ASSIGNED = "ASSIGNED";
    private static final String ALREADY_ASSIGNED = "ALREADY_ASSIGNED";

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final Set<String> allowedPermissions;

    public UserPermissionAssignmentServiceImpl(
        UserRepository userRepository,
        PermissionRepository permissionRepository,
        @Value("${app.permissions.allowed}") String allowedPermissions
    ) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.allowedPermissions = Arrays.stream(allowedPermissions.split(","))
            .map(String::trim)
            .filter(permission -> !permission.isBlank())
            .map(permission -> permission.toUpperCase(Locale.ROOT))
            .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public PermissionAssignmentResponse assignPermission(Long userId, String permission) {
        String normalizedPermission = permission.trim().toUpperCase(Locale.ROOT);
        if (!allowedPermissions.contains(normalizedPermission)) {
            throw new InvalidPermissionException(normalizedPermission);
        }

        UserEntity userEntity = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        boolean alreadyAssigned = userEntity.getDirectPermissions()
            .stream()
            .map(PermissionEntity::getCode)
            .map(code -> code.toUpperCase(Locale.ROOT))
            .anyMatch(normalizedPermission::equals);
        if (alreadyAssigned) {
            return new PermissionAssignmentResponse(userId, normalizedPermission, ALREADY_ASSIGNED);
        }

        PermissionEntity permissionEntity = permissionRepository.findByCode(normalizedPermission)
            .orElseGet(() -> permissionRepository.save(new PermissionEntity(normalizedPermission)));
        userEntity.addDirectPermission(permissionEntity);
        userRepository.save(userEntity);
        return new PermissionAssignmentResponse(userId, normalizedPermission, ASSIGNED);
    }
}
