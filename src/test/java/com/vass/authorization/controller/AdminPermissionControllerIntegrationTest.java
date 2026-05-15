package com.vass.authorization.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vass.authorization.entity.PermissionEntity;
import com.vass.authorization.entity.RoleEntity;
import com.vass.authorization.entity.UserEntity;
import com.vass.authorization.repository.PermissionRepository;
import com.vass.authorization.repository.RoleRepository;
import com.vass.authorization.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPermissionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    @Test
    void testReplacePermissions_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        UserEntity user = userRepository.save(new UserEntity(true));

        mockMvc.perform(put("/api/admin/users/{userId}/permissions", user.getId())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"permissions":["REPORT:READ"]}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testReplacePermissions_WithoutAdminPermission_ReturnsForbidden() throws Exception {
        UserEntity user = userRepository.save(new UserEntity(true));
        String token = token("user-2", List.of("REPORT:READ"), Instant.now().plusSeconds(3600));

        mockMvc.perform(put("/api/admin/users/{userId}/permissions", user.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"permissions":["REPORT:READ"]}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void testReplacePermissions_AdminRequest_ReturnsUpdated() throws Exception {
        UserEntity user = createUserWithPermissions("REPORT:DOWNLOAD");
        String token = token("admin-1", List.of("ADMIN:MANAGE_PERMISSIONS"), Instant.now().plusSeconds(3600));

        mockMvc.perform(put("/api/admin/users/{userId}/permissions", user.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"permissions":["REPORT_READ","REPORT_DOWNLOAD","REPORT_READ"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UPDATED"))
                .andExpect(jsonPath("$.permissions.length()").value(2))
                .andExpect(jsonPath("$.permissions[0]").value("REPORT:DOWNLOAD"))
                .andExpect(jsonPath("$.permissions[1]").value("REPORT:READ"));

        mockMvc.perform(get("/api/permissions/users/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions.length()").value(2));
    }

    @Test
    void testReplacePermissions_InvalidRequest_ReturnsBadRequest() throws Exception {
        UserEntity user = userRepository.save(new UserEntity(true));
        String token = token("admin-1", List.of("ADMIN:MANAGE_PERMISSIONS"), Instant.now().plusSeconds(3600));

        mockMvc.perform(put("/api/admin/users/{userId}/permissions", user.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"permissions":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void testReplacePermissions_UserNotFound_ReturnsNotFound() throws Exception {
        String token = token("admin-1", List.of("ADMIN:MANAGE_PERMISSIONS"), Instant.now().plusSeconds(3600));

        mockMvc.perform(put("/api/admin/users/{userId}/permissions", 99999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"permissions":["REPORT:READ"]}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    private UserEntity createUserWithPermissions(String permissionCode) {
        PermissionEntity permission = permissionRepository.save(new PermissionEntity(permissionCode));
        RoleEntity role = new RoleEntity("INIT_ROLE_" + permissionCode.replace(':', '_'));
        role.setPermissions(Set.of(permission));
        roleRepository.save(role);

        UserEntity user = new UserEntity(true);
        user.setRoles(Set.of(role));
        return userRepository.save(user);
    }

    private String token(String subject, List<String> permissions, Instant expiration) {
        return Jwts.builder()
                .subject(subject)
                .claim("permissions", permissions)
                .expiration(Date.from(expiration))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
