package com.vass.authorization.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vass.authorization.entity.AuditLogEntity;
import com.vass.authorization.entity.PermissionEntity;
import com.vass.authorization.entity.UserEntity;
import com.vass.authorization.repository.AuditLogRepository;
import com.vass.authorization.repository.PermissionRepository;
import com.vass.authorization.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPermissionsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Value("${app.security.jwt-secret}")
    private String jwtSecret;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void should_return_200_when_admin_replaces_permissions_successfully() throws Exception {
        UserEntity targetUser = createUserWithDirectPermissions(
            "admin-replace-success",
            Set.of("REPORT:READ")
        );
        String token = createToken("admin-user", Set.of("ADMIN"), Instant.now().plusSeconds(600));

        mockMvc.perform(
                put("/api/admin/users/" + targetUser.getId() + "/permissions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "permissions": ["REPORT:DOWNLOAD", "REPORT:READ", "REPORT:READ"]
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(targetUser.getId()))
            .andExpect(jsonPath("$.status").value("UPDATED"))
            .andExpect(jsonPath("$.permissions.length()").value(2));

        Set<String> storedPermissions = userRepository.findDirectPermissionCodesByUserId(targetUser.getId());
        assertThat(storedPermissions).containsExactlyInAnyOrder("REPORT:DOWNLOAD", "REPORT:READ");

        List<AuditLogEntity> auditEntries = auditLogRepository.findAll();
        assertThat(auditEntries).hasSize(1);
        AuditLogEntity auditLogEntity = auditEntries.get(0);
        assertThat(auditLogEntity.getActor()).isEqualTo("admin-user");
        assertThat(auditLogEntity.getTargetUserId()).isEqualTo(targetUser.getId());
        assertThat(auditLogEntity.getBeforePermissions()).isEqualTo("REPORT:READ");
        assertThat(auditLogEntity.getAfterPermissions()).isEqualTo("REPORT:DOWNLOAD,REPORT:READ");
    }

    @Test
    void should_return_200_when_admin_sends_empty_permissions_and_revokes_all() throws Exception {
        UserEntity targetUser = createUserWithDirectPermissions(
            "admin-revoke-all",
            Set.of("REPORT:READ", "REPORT:DOWNLOAD")
        );
        String token = createToken("admin-user", Set.of("ADMIN"), Instant.now().plusSeconds(600));

        mockMvc.perform(
                put("/api/admin/users/" + targetUser.getId() + "/permissions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "permissions": []
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(targetUser.getId()))
            .andExpect(jsonPath("$.status").value("UPDATED"))
            .andExpect(jsonPath("$.permissions").isArray())
            .andExpect(jsonPath("$.permissions").isEmpty());

        Set<String> storedPermissions = userRepository.findDirectPermissionCodesByUserId(targetUser.getId());
        assertThat(storedPermissions).isEmpty();
    }

    @Test
    void should_return_401_when_token_is_missing() throws Exception {
        mockMvc.perform(
                put("/api/admin/users/1/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "permissions": ["REPORT:READ"]
                        }
                        """)
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.path").value("/api/admin/users/1/permissions"));
    }

    @Test
    void should_return_403_when_token_is_valid_but_user_is_not_admin() throws Exception {
        UserEntity targetUser = createUserWithDirectPermissions(
            "admin-forbidden-target",
            Set.of("REPORT:READ")
        );
        String token = createToken("regular-user", Set.of("REPORT:READ"), Instant.now().plusSeconds(600));

        mockMvc.perform(
                put("/api/admin/users/" + targetUser.getId() + "/permissions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "permissions": ["REPORT:DOWNLOAD"]
                        }
                        """)
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.path").value("/api/admin/users/" + targetUser.getId() + "/permissions"));
    }

    @Test
    void should_return_400_when_permissions_are_invalid() throws Exception {
        UserEntity targetUser = createUserWithDirectPermissions(
            "admin-invalid-permission-target",
            Set.of("REPORT:READ")
        );
        String token = createToken("admin-user", Set.of("ADMIN"), Instant.now().plusSeconds(600));

        mockMvc.perform(
                put("/api/admin/users/" + targetUser.getId() + "/permissions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "permissions": ["UNKNOWN:PERMISSION"]
                        }
                        """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.path").value("/api/admin/users/" + targetUser.getId() + "/permissions"));
    }

    @Test
    void should_return_404_when_target_user_does_not_exist() throws Exception {
        String token = createToken("admin-user", Set.of("ADMIN"), Instant.now().plusSeconds(600));

        mockMvc.perform(
                put("/api/admin/users/999999/permissions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "permissions": ["REPORT:READ"]
                        }
                        """)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.path").value("/api/admin/users/999999/permissions"));
    }

    private UserEntity createUserWithDirectPermissions(String username, Set<String> permissions) {
        UserEntity userEntity = userRepository.save(new UserEntity(username, true));
        permissions.forEach(permission -> {
            PermissionEntity permissionEntity = permissionRepository.findByCode(permission)
                .orElseGet(() -> permissionRepository.save(new PermissionEntity(permission)));
            userEntity.addDirectPermission(permissionEntity);
        });
        return userRepository.save(userEntity);
    }

    private String createToken(String subject, Set<String> permissions, Instant expiration) throws Exception {
        String headerJson = objectMapper.writeValueAsString(Map.of("alg", "HS256", "typ", "JWT"));
        String payloadJson = objectMapper.writeValueAsString(
            Map.of(
                "sub",
                subject,
                "permissions",
                permissions,
                "exp",
                expiration.getEpochSecond()
            )
        );

        String encodedHeader = encodeSection(headerJson);
        String encodedPayload = encodeSection(payloadJson);
        String signature = sign(encodedHeader + "." + encodedPayload);
        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
    }

    private String encodeSection(String json) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}
