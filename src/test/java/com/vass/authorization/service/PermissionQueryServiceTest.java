package com.vass.authorization.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.vass.authorization.entity.PermissionEntity;
import com.vass.authorization.entity.RoleEntity;
import com.vass.authorization.entity.UserEntity;
import com.vass.authorization.exception.ResourceNotFoundException;
import com.vass.authorization.repository.UserRepository;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionQueryServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PermissionQueryService permissionQueryService;

    @Test
    void testGetUserPermissions_UserWithDuplicatedPermissions_ReturnsDeduplicatedList() {
        UserEntity user = new UserEntity(true);
        RoleEntity roleA = new RoleEntity("A");
        setEntityId(roleA, 1L);
        PermissionEntity permissionDownloadLower = new PermissionEntity("report:download");
        setEntityId(permissionDownloadLower, 10L);
        PermissionEntity permissionReadUpper = new PermissionEntity("REPORT:READ");
        setEntityId(permissionReadUpper, 11L);
        roleA.setPermissions(Set.of(
                permissionDownloadLower,
                permissionReadUpper
        ));
        RoleEntity roleB = new RoleEntity("B");
        setEntityId(roleB, 2L);
        PermissionEntity permissionDownloadUpper = new PermissionEntity("REPORT:DOWNLOAD");
        setEntityId(permissionDownloadUpper, 12L);
        roleB.setPermissions(Set.of(
                permissionDownloadUpper
        ));
        user.setRoles(Set.of(roleA, roleB));

        when(userRepository.findDetailedById(10L)).thenReturn(Optional.of(user));

        var response = permissionQueryService.getUserPermissions(10L);

        assertEquals(10L, response.userId());
        assertEquals(2, response.permissions().size());
        assertEquals("REPORT:DOWNLOAD", response.permissions().get(0));
        assertEquals("REPORT:READ", response.permissions().get(1));
    }

    @Test
    void testGetUserPermissions_UserWithoutRoles_ReturnsEmptyList() {
        UserEntity user = new UserEntity(true);
        user.setRoles(Set.of());

        when(userRepository.findDetailedById(11L)).thenReturn(Optional.of(user));

        var response = permissionQueryService.getUserPermissions(11L);

        assertEquals(11L, response.userId());
        assertEquals(0, response.permissions().size());
    }

    @Test
    void testGetUserPermissions_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findDetailedById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> permissionQueryService.getUserPermissions(999L));
    }

    private void setEntityId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("No fue posible preparar entidad de prueba", ex);
        }
    }
}
