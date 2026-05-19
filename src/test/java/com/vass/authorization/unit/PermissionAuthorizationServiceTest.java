package com.vass.authorization.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vass.authorization.api.dto.request.PermissionCheckRequest;
import com.vass.authorization.entity.UserEntity;
import com.vass.authorization.repository.UserRepository;
import com.vass.authorization.service.AuditService;
import com.vass.authorization.service.impl.PermissionAuthorizationService;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionAuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private PermissionAuthorizationService permissionAuthorizationService;

    @Test
    void should_allow_when_user_has_permission() {
        UserEntity activeUser = new UserEntity("active-user", true);
        PermissionCheckRequest request = new PermissionCheckRequest(10L, "report", "download");

        when(userRepository.findById(10L)).thenReturn(Optional.of(activeUser));
        when(userRepository.findActivePermissionCodesByUserId(10L)).thenReturn(Set.of("REPORT:DOWNLOAD"));

        boolean allowed = permissionAuthorizationService.checkPermission(request);

        assertThat(allowed).isTrue();
        verify(auditService, never()).registerDeniedPermissionCheck(10L, "report", "download");
    }

    @Test
    void should_deny_when_user_is_inactive() {
        UserEntity inactiveUser = new UserEntity("inactive-user", false);
        PermissionCheckRequest request = new PermissionCheckRequest(11L, "invoice", "read");

        when(userRepository.findById(11L)).thenReturn(Optional.of(inactiveUser));

        boolean allowed = permissionAuthorizationService.checkPermission(request);

        assertThat(allowed).isFalse();
        verify(auditService).registerDeniedPermissionCheck(11L, "invoice", "read");
    }

    @Test
    void should_deny_when_permission_is_missing() {
        UserEntity activeUser = new UserEntity("active-user", true);
        PermissionCheckRequest request = new PermissionCheckRequest(12L, "report", "download");

        when(userRepository.findById(12L)).thenReturn(Optional.of(activeUser));
        when(userRepository.findActivePermissionCodesByUserId(12L)).thenReturn(Set.of("REPORT:READ"));

        boolean allowed = permissionAuthorizationService.checkPermission(request);

        assertThat(allowed).isFalse();
        verify(auditService).registerDeniedPermissionCheck(12L, "report", "download");
    }
}
