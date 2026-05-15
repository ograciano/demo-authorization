package com.vass.authorization.service;

import com.vass.authorization.dto.AuthorizationResponseDTO;
import com.vass.authorization.dto.CheckPermissionRequestDTO;
import com.vass.authorization.entity.PermissionEntity;
import com.vass.authorization.entity.RoleEntity;
import com.vass.authorization.entity.UserEntity;
import com.vass.authorization.repository.UserRepository;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationService.class);

    private final UserRepository userRepository;

    public AuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthorizationResponseDTO checkPermission(CheckPermissionRequestDTO request) {
        String normalizedResource = normalizeSegment(request.resource());
        String normalizedAction = normalizeSegment(request.action());
        String requiredPermission = normalizedResource + ":" + normalizedAction;

        UserEntity user = userRepository.findDetailedById(request.userId()).orElse(null);
        if (user == null) {
            return denied(request.userId(), normalizedResource, normalizedAction, "Usuario no encontrado");
        }

        if (!user.isActive()) {
            return denied(request.userId(), normalizedResource, normalizedAction, "Usuario inactivo");
        }

        Set<RoleEntity> roles = user.getRoles() == null ? Collections.emptySet() : user.getRoles();
        if (roles.isEmpty()) {
            return denied(request.userId(), normalizedResource, normalizedAction, "Usuario sin roles asignados");
        }

        boolean allowed = roles.stream()
                .flatMap(this::permissionsOfRole)
                .map(PermissionEntity::getCode)
                .filter(code -> code != null && !code.isBlank())
                .map(this::normalizeSegment)
                .anyMatch(requiredPermission::equals);

        if (!allowed) {
            return denied(request.userId(), normalizedResource, normalizedAction, "Permiso denegado");
        }

        return new AuthorizationResponseDTO(
                true,
                request.userId(),
                normalizedResource,
                normalizedAction,
                "Permiso concedido"
        );
    }

    private AuthorizationResponseDTO denied(Long userId, String resource, String action, String reason) {
        log.warn("Authorization denied userId={} permission={}:{} reason={}", userId, resource, action, reason);
        return new AuthorizationResponseDTO(false, userId, resource, action, reason);
    }

    private Stream<PermissionEntity> permissionsOfRole(RoleEntity role) {
        if (role == null || role.getPermissions() == null) {
            return Stream.empty();
        }
        return role.getPermissions().stream();
    }

    private String normalizeSegment(String segment) {
        return segment.trim().toUpperCase(Locale.ROOT);
    }
}
