package com.vass.authorization.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class PermissionAuthorizationServiceTest {

    private final PermissionAuthorizationService permissionAuthorizationService = new PermissionAuthorizationService();

    @Test
    void testHasPermission_WithRequiredAuthority_ReturnsTrue() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "user-1",
                null,
                List.of(new SimpleGrantedAuthority("PERM_REPORT:DOWNLOAD"))
        );

        boolean result = permissionAuthorizationService.hasPermission(authentication, "report:download");

        assertTrue(result);
    }

    @Test
    void testHasPermission_WithoutRequiredAuthority_ReturnsFalse() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "user-1",
                null,
                List.of(new SimpleGrantedAuthority("PERM_REPORT:READ"))
        );

        boolean result = permissionAuthorizationService.hasPermission(authentication, "REPORT:DOWNLOAD");

        assertFalse(result);
    }
}
