package com.vass.authorization.config.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final String JWT_TYPE = "JWT";
    private static final String HEADER_TYP = "typ";
    private static final String HEADER_ALG = "alg";
    private static final String PAYLOAD_SUB = "sub";
    private static final String PAYLOAD_EXP = "exp";
    private static final String PAYLOAD_PERMISSIONS = "permissions";

    private final ObjectMapper objectMapper;
    private final String jwtSecret;

    public JwtTokenService(
        ObjectMapper objectMapper,
        @Value("${app.security.jwt-secret}") String jwtSecret
    ) {
        this.objectMapper = objectMapper;
        this.jwtSecret = jwtSecret;
    }

    public AuthenticatedJwt parseAndValidate(String rawToken) throws AuthenticationException {
        String[] parts = rawToken.split("\\.");
        if (parts.length != 3) {
            throw new BadCredentialsException("Malformed JWT");
        }

        Map<String, Object> header = decodeJson(parts[0], "header");
        validateHeader(header);

        verifySignature(parts[0], parts[1], parts[2]);

        Map<String, Object> payload = decodeJson(parts[1], "payload");
        validateExpiration(payload);

        String subject = String.valueOf(payload.getOrDefault(PAYLOAD_SUB, "anonymous"));
        Set<String> permissions = extractPermissions(payload.get(PAYLOAD_PERMISSIONS));
        return new AuthenticatedJwt(subject, permissions);
    }

    private Map<String, Object> decodeJson(String section, String sectionName) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(section);
            return objectMapper.readValue(decodedBytes, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new BadCredentialsException("Invalid JWT " + sectionName, exception);
        }
    }

    private void validateHeader(Map<String, Object> header) {
        Object type = header.get(HEADER_TYP);
        Object alg = header.get(HEADER_ALG);
        if (!JWT_TYPE.equals(type) || !"HS256".equals(alg)) {
            throw new BadCredentialsException("Unsupported JWT header");
        }
    }

    private void verifySignature(String headerPart, String payloadPart, String providedSignaturePart) {
        try {
            String message = headerPart + "." + payloadPart;
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256);
            mac.init(secretKeySpec);
            byte[] expectedSignature = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            byte[] providedSignature = Base64.getUrlDecoder().decode(providedSignaturePart);
            if (!MessageDigest.isEqual(expectedSignature, providedSignature)) {
                throw new BadCredentialsException("Invalid JWT signature");
            }
        } catch (BadCredentialsException badCredentialsException) {
            throw badCredentialsException;
        } catch (Exception exception) {
            throw new BadCredentialsException("Invalid JWT signature", exception);
        }
    }

    private void validateExpiration(Map<String, Object> payload) {
        Object expClaim = payload.get(PAYLOAD_EXP);
        if (!(expClaim instanceof Number expNumber)) {
            throw new BadCredentialsException("Missing exp claim");
        }
        long expirationEpochSeconds = expNumber.longValue();
        if (expirationEpochSeconds < Instant.now().getEpochSecond()) {
            throw new CredentialsExpiredException("Expired JWT");
        }
    }

    private Set<String> extractPermissions(Object permissionsClaim) {
        Set<String> permissions = new LinkedHashSet<>();
        if (permissionsClaim instanceof List<?> permissionList) {
            for (Object item : permissionList) {
                if (item instanceof String permission && !permission.isBlank()) {
                    permissions.add(permission.trim());
                }
            }
        }
        return permissions;
    }

    public record AuthenticatedJwt(String subject, Set<String> permissions) {
    }
}
