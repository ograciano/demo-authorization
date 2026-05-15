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
import com.vass.authorization.service.AdminPermissionUpdateHook;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AdminPermissionRollbackIntegrationTest.FailHookConfig.class)
class AdminPermissionRollbackIntegrationTest {

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
    void testReplacePermissions_WhenFailureOccurs_RollsBackChanges() throws Exception {
        UserEntity user = createUserWithPermissions("REPORT:DOWNLOAD");
        String token = token("admin-1", List.of("ADMIN:MANAGE_PERMISSIONS"), Instant.now().plusSeconds(3600));

        mockMvc.perform(put("/api/admin/users/{userId}/permissions", user.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"permissions":["REPORT:READ"]}
                                """))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(get("/api/permissions/users/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions.length()").value(1))
                .andExpect(jsonPath("$.permissions[0]").value("REPORT:DOWNLOAD"));
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

    @TestConfiguration
    static class FailHookConfig {
        @Bean
        @Primary
        AdminPermissionUpdateHook failingHook() {
            return (userId, previousPermissions, updatedPermissions) -> {
                throw new IllegalStateException("forced rollback");
            };
        }
    }
}
