package com.vass.authorization.service;

import com.vass.authorization.dto.AssignPermissionResponseDTO;
import com.vass.authorization.entity.PermissionEntity;
import com.vass.authorization.entity.RoleEntity;
import com.vass.authorization.entity.UserEntity;
import com.vass.authorization.exception.ResourceNotFoundException;
import com.vass.authorization.repository.PermissionRepository;
import com.vass.authorization.repository.RoleRepository;
import com.vass.authorization.repository.UserRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionAssignmentService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final Set<String> allowedPermissions;

    public PermissionAssignmentService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            @Value("${authorization.permissions.allowed}") String allowedPermissionsValue
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.allowedPermissions = Arrays.stream(allowedPermissionsValue.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    @Transactional
    public AssignPermissionResponseDTO assignPermission(Long userId, String requestedPermission) {
        String normalizedPermission = normalizePermission(requestedPermission);
        validatePermissionCatalog(normalizedPermission);

        UserEntity user = userRepository.findDetailedById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (userAlreadyHasPermission(user, normalizedPermission)) {
            return buildResponse(userId, normalizedPermission, "ALREADY_ASSIGNED");
        }

        PermissionEntity permission = permissionRepository.findByCodeIgnoreCase(normalizedPermission)
                .orElseThrow(() -> new IllegalArgumentException("Permiso invalido o fuera de catalogo"));

        RoleEntity role = roleRepository.findFirstByPermissions_CodeIgnoreCase(normalizedPermission)
                .orElseThrow(() -> new IllegalArgumentException("Permiso sin rol configurado"));

        role.getPermissions().add(permission);
        roleRepository.save(role);

        user.getRoles().add(role);
        userRepository.save(user);

        return buildResponse(userId, normalizedPermission, "ASSIGNED");
    }

    private boolean userAlreadyHasPermission(UserEntity user, String permission) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(PermissionEntity::getCode)
                .filter(code -> code != null && !code.isBlank())
                .map(this::normalizePermission)
                .anyMatch(permission::equals);
    }

    private void validatePermissionCatalog(String permission) {
        if (!allowedPermissions.contains(permission)) {
            throw new IllegalArgumentException("Permiso invalido o fuera de catalogo");
        }
    }

    private String normalizePermission(String permission) {
        return permission == null ? "" : permission.trim().toUpperCase(Locale.ROOT);
    }

    private AssignPermissionResponseDTO buildResponse(Long userId, String permission, String status) {
        return new AssignPermissionResponseDTO(userId, permission, status, Instant.now());
    }
}
