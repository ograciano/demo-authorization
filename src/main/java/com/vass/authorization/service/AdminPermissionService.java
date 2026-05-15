package com.vass.authorization.service;

import com.vass.authorization.dto.AdminReplacePermissionsResponseDTO;
import com.vass.authorization.entity.PermissionEntity;
import com.vass.authorization.entity.RoleEntity;
import com.vass.authorization.entity.UserEntity;
import com.vass.authorization.exception.ResourceNotFoundException;
import com.vass.authorization.repository.PermissionRepository;
import com.vass.authorization.repository.RoleRepository;
import com.vass.authorization.repository.UserRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminPermissionService {

    private static final Logger log = LoggerFactory.getLogger(AdminPermissionService.class);
    private static final String ADMIN_PERMISSION = "ADMIN:MANAGE_PERMISSIONS";
    private static final String UPDATED_STATUS = "UPDATED";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AdminPermissionUpdateHook updateHook;
    private final Set<String> allowedPermissions;

    public AdminPermissionService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            AdminPermissionUpdateHook updateHook,
            @Value("${authorization.permissions.allowed}") String allowedPermissionsValue
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.updateHook = updateHook;
        this.allowedPermissions = Arrays.stream(allowedPermissionsValue.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    @Transactional
    public AdminReplacePermissionsResponseDTO replacePermissions(Long userId, List<String> requestedPermissions) {
        if (requestedPermissions == null) {
            throw new IllegalArgumentException("El campo permissions es obligatorio");
        }

        Set<String> normalizedPermissions = requestedPermissions.stream()
                .map(this::normalizePermission)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalizedPermissions.size() != requestedPermissions.size()) {
            log.info("Deduplicated permissions for userId={} originalCount={} deduplicatedCount={}",
                    userId, requestedPermissions.size(), normalizedPermissions.size());
        }

        validatePermissionCatalog(normalizedPermissions);
        validateSelfEscalation(userId, normalizedPermissions);

        UserEntity user = userRepository.findDetailedById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Set<String> previousPermissions = extractEffectivePermissions(user);
        RoleEntity directRole = roleRepository.findByName(directRoleName(userId))
                .orElseGet(() -> roleRepository.save(new RoleEntity(directRoleName(userId))));

        Set<PermissionEntity> targetPermissions = normalizedPermissions.stream()
                .map(permission -> permissionRepository.findByCodeIgnoreCase(permission)
                        .orElseGet(() -> permissionRepository.save(new PermissionEntity(permission))))
                .collect(Collectors.toCollection(HashSet::new));

        directRole.setPermissions(targetPermissions);
        roleRepository.save(directRole);

        user.setRoles(new HashSet<>(Set.of(directRole)));
        userRepository.save(user);

        Set<String> updatedPermissions = new HashSet<>(normalizedPermissions);
        updateHook.beforeCommit(userId, previousPermissions, updatedPermissions);

        String actor = currentActor();
        log.info(
                "Admin permissions replaced actor={} targetUserId={} previousPermissions={} updatedPermissions={}",
                actor, userId, previousPermissions, updatedPermissions
        );

        return new AdminReplacePermissionsResponseDTO(
                userId,
                updatedPermissions.stream().sorted().toList(),
                UPDATED_STATUS,
                Instant.now()
        );
    }

    private Set<String> extractEffectivePermissions(UserEntity user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(PermissionEntity::getCode)
                .filter(code -> code != null && !code.isBlank())
                .map(this::normalizePermission)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private void validatePermissionCatalog(Set<String> permissions) {
        for (String permission : permissions) {
            if (!allowedPermissions.contains(permission)) {
                throw new IllegalArgumentException("Permiso invalido o fuera de catalogo");
            }
        }
    }

    private void validateSelfEscalation(Long userId, Set<String> permissions) {
        String actor = currentActor();
        if (actor.equalsIgnoreCase("user-" + userId) && permissions.contains(ADMIN_PERMISSION)) {
            throw new AccessDeniedException("No se permite auto-escalamiento de privilegios");
        }
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "UNKNOWN";
        }
        return authentication.getName();
    }

    private String directRoleName(Long userId) {
        return "USER_" + userId + "_DIRECT";
    }

    private String normalizePermission(String permission) {
        if (permission == null) {
            return "";
        }
        String normalized = permission.trim().toUpperCase(Locale.ROOT);
        if (!normalized.contains(":")) {
            int separatorIndex = normalized.indexOf('_');
            if (separatorIndex >= 0) {
                normalized = normalized.substring(0, separatorIndex)
                        + ":"
                        + normalized.substring(separatorIndex + 1);
            }
        }
        return normalized;
    }
}
