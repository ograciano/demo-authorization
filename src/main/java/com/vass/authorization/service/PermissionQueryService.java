package com.vass.authorization.service;

import com.vass.authorization.dto.UserPermissionsResponseDTO;
import com.vass.authorization.entity.PermissionEntity;
import com.vass.authorization.entity.RoleEntity;
import com.vass.authorization.entity.UserEntity;
import com.vass.authorization.exception.ResourceNotFoundException;
import com.vass.authorization.repository.UserRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class PermissionQueryService {

    private final UserRepository userRepository;

    public PermissionQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserPermissionsResponseDTO getUserPermissions(Long userId) {
        UserEntity user = userRepository.findDetailedById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Set<RoleEntity> roles = user.getRoles() == null ? Collections.emptySet() : user.getRoles();
        List<String> permissions = roles.stream()
                .flatMap(this::permissionsOfRole)
                .map(PermissionEntity::getCode)
                .filter(code -> code != null && !code.isBlank())
                .map(this::normalizePermission)
                .distinct()
                .sorted()
                .toList();

        return new UserPermissionsResponseDTO(userId, permissions, Instant.now());
    }

    private Stream<PermissionEntity> permissionsOfRole(RoleEntity role) {
        if (role == null || role.getPermissions() == null) {
            return Stream.empty();
        }
        return role.getPermissions().stream();
    }

    private String normalizePermission(String permission) {
        return permission.trim().toUpperCase(Locale.ROOT);
    }
}
