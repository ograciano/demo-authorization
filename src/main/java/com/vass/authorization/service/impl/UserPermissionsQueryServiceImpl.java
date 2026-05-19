package com.vass.authorization.service.impl;

import com.vass.authorization.api.dto.response.UserPermissionsResponse;
import com.vass.authorization.repository.UserRepository;
import com.vass.authorization.service.UserPermissionsQueryService;
import com.vass.authorization.service.exception.UserNotFoundException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPermissionsQueryServiceImpl implements UserPermissionsQueryService {

    private final UserRepository userRepository;

    public UserPermissionsQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserPermissionsResponse getUserPermissions(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        Set<String> permissionCodes = new HashSet<>(userRepository.findActivePermissionCodesByUserId(userId));
        permissionCodes.addAll(userRepository.findDirectPermissionCodesByUserId(userId));
        List<String> permissions = permissionCodes
            .stream()
            .sorted()
            .toList();
        return new UserPermissionsResponse(userId, permissions, Instant.now());
    }
}
