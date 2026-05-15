package com.vass.authorization.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionAssignmentServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    private PermissionAssignmentService permissionAssignmentService;

    @BeforeEach
    void setUp() {
        permissionAssignmentService = new PermissionAssignmentService(
                userRepository,
                roleRepository,
                permissionRepository,
                "REPORT:READ,REPORT:DOWNLOAD"
        );
    }

    @Test
    void testAssignPermission_NewPermission_ReturnsAssigned() {
        UserEntity user = new UserEntity(true);
        PermissionEntity permission = new PermissionEntity("REPORT:DOWNLOAD");
        RoleEntity role = new RoleEntity("REPORT_DOWNLOADER");
        role.getPermissions().add(permission);

        when(userRepository.findDetailedById(1L)).thenReturn(Optional.of(user));
        when(permissionRepository.findByCodeIgnoreCase("REPORT:DOWNLOAD")).thenReturn(Optional.of(permission));
        when(roleRepository.findFirstByPermissions_CodeIgnoreCase("REPORT:DOWNLOAD")).thenReturn(Optional.of(role));
        when(roleRepository.save(any(RoleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = permissionAssignmentService.assignPermission(1L, "report:download");

        assertEquals("ASSIGNED", response.status());
        assertEquals("REPORT:DOWNLOAD", response.permission());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void testAssignPermission_AlreadyAssigned_ReturnsAlreadyAssigned() {
        PermissionEntity permission = new PermissionEntity("REPORT:DOWNLOAD");
        RoleEntity role = new RoleEntity("ROLE_READER");
        role.setPermissions(Set.of(permission));
        UserEntity user = new UserEntity(true);
        user.setRoles(Set.of(role));

        when(userRepository.findDetailedById(2L)).thenReturn(Optional.of(user));

        var response = permissionAssignmentService.assignPermission(2L, "REPORT:DOWNLOAD");

        assertEquals("ALREADY_ASSIGNED", response.status());
    }

    @Test
    void testAssignPermission_InvalidPermission_ThrowsBadRequest() {
        assertThrows(IllegalArgumentException.class,
                () -> permissionAssignmentService.assignPermission(3L, "INVALID:PERMISSION"));
    }

    @Test
    void testAssignPermission_UserNotFound_ThrowsNotFound() {
        when(userRepository.findDetailedById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> permissionAssignmentService.assignPermission(999L, "REPORT:DOWNLOAD"));
    }
}
