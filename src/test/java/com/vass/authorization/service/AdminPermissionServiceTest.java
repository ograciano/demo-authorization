package com.vass.authorization.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.vass.authorization.entity.PermissionEntity;
import com.vass.authorization.entity.RoleEntity;
import com.vass.authorization.entity.UserEntity;
import com.vass.authorization.exception.ResourceNotFoundException;
import com.vass.authorization.repository.PermissionRepository;
import com.vass.authorization.repository.RoleRepository;
import com.vass.authorization.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AdminPermissionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    private AdminPermissionService adminPermissionService;

    @BeforeEach
    void setUp() {
        adminPermissionService = new AdminPermissionService(
                userRepository,
                roleRepository,
                permissionRepository,
                (userId, previousPermissions, updatedPermissions) -> {
                },
                "REPORT:READ,REPORT:DOWNLOAD,ADMIN:MANAGE_PERMISSIONS"
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testReplacePermissions_ValidRequest_ReturnsUpdated() {
        setActor("admin-1");
        UserEntity user = new UserEntity(true);

        when(userRepository.findDetailedById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("USER_1_DIRECT")).thenReturn(Optional.empty());
        when(roleRepository.save(any(RoleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(permissionRepository.findByCodeIgnoreCase("REPORT:READ")).thenReturn(Optional.empty());
        when(permissionRepository.save(any(PermissionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = adminPermissionService.replacePermissions(1L, java.util.List.of("REPORT_READ"));

        assertEquals("UPDATED", response.status());
        assertEquals(1, response.permissions().size());
        assertEquals("REPORT:READ", response.permissions().get(0));
    }

    @Test
    void testReplacePermissions_InvalidCatalog_ThrowsBadRequest() {
        setActor("admin-1");

        assertThrows(IllegalArgumentException.class,
                () -> adminPermissionService.replacePermissions(1L, java.util.List.of("INVALID:PERMISSION")));
    }

    @Test
    void testReplacePermissions_UserNotFound_ThrowsNotFound() {
        setActor("admin-1");
        when(userRepository.findDetailedById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> adminPermissionService.replacePermissions(999L, java.util.List.of("REPORT:READ")));
    }

    @Test
    void testReplacePermissions_SelfEscalation_ThrowsAccessDenied() {
        setActor("user-7");

        assertThrows(AccessDeniedException.class,
                () -> adminPermissionService.replacePermissions(7L, java.util.List.of("ADMIN:MANAGE_PERMISSIONS")));
    }

    private void setActor(String actor) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actor, null, java.util.List.of())
        );
    }
}
