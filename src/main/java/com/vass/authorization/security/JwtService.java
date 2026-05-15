package com.vass.authorization.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private final byte[] secret;

    public JwtService(@Value("${security.jwt.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public Authentication toAuthentication(String token) {
        Claims claims = parseClaims(token);
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new JwtException("Token sin subject");
        }

        Collection<GrantedAuthority> authorities = extractAuthorities(claims);
        return new UsernamePasswordAuthenticationToken(subject, null, authorities);
    }

    public Claims parseClaims(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Instant expiration = claims.getExpiration() == null ? null : claims.getExpiration().toInstant();
        if (expiration == null || expiration.isBefore(Instant.now())) {
            throw new JwtException("Token expirado");
        }

        return claims;
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Claims claims) {
        Object rawPermissions = claims.get("permissions");
        if (!(rawPermissions instanceof List<?> list)) {
            return List.of();
        }

        Set<String> normalizedPermissions = new HashSet<>();
        for (Object item : list) {
            if (item instanceof String permission && !permission.isBlank()) {
                normalizedPermissions.add(permission.trim().toUpperCase(Locale.ROOT));
            }
        }

        return normalizedPermissions.stream()
                .map(permission -> new SimpleGrantedAuthority("PERM_" + permission))
                .map(authority -> (GrantedAuthority) authority)
                .toList();
    }
}
