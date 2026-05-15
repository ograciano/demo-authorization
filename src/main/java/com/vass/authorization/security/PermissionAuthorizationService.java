package com.vass.authorization.security;

import java.util.Locale;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class PermissionAuthorizationService {

    public boolean hasPermission(Authentication authentication, String permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String requiredAuthority = "PERM_" + permission.trim().toUpperCase(Locale.ROOT);
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (requiredAuthority.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
