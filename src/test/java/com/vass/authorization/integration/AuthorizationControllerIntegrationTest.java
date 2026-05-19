package com.vass.authorization.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vass.authorization.entity.PermissionEntity;
import com.vass.authorization.entity.RoleEntity;
import com.vass.authorization.entity.UserEntity;
import com.vass.authorization.repository.AuditLogRepository;
import com.vass.authorization.repository.UserRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void should_return_allowed_true_when_user_has_permission() throws Exception {
        UserEntity user = createUserWithPermission(
            "user-allowed",
            true,
            "ROLE_REPORT_ALLOWED",
            true,
            "REPORT:DOWNLOAD"
        );

        mockMvc.perform(
                post("/api/authorization/check-permission")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "userId", user.getId(),
                        "resource", "report",
                        "action", "download"
                    )))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allowed").value(true));
    }

    @Test
    void should_return_bad_request_when_request_is_invalid() throws Exception {
        mockMvc.perform(
                post("/api/authorization/check-permission")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "userId": 0,
                          "resource": "",
                          "action": " "
                        }
                        """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.path").value("/api/authorization/check-permission"));
    }

    @Test
    void should_register_audit_when_permission_is_denied() throws Exception {
        UserEntity user = createUserWithPermission(
            "user-denied",
            true,
            "ROLE_REPORT_DENIED",
            true,
            "REPORT:READ"
        );

        mockMvc.perform(
                post("/api/authorization/check-permission")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "userId", user.getId(),
                        "resource", "report",
                        "action", "download"
                    )))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allowed").value(false));

        org.assertj.core.api.Assertions.assertThat(auditLogRepository.count()).isEqualTo(1L);
        org.assertj.core.api.Assertions.assertThat(auditLogRepository.findAll().get(0).getUserId()).isEqualTo(user.getId());
    }

    private UserEntity createUserWithPermission(
        String username,
        boolean activeUser,
        String roleName,
        boolean activeRole,
        String permissionCode
    ) {
        PermissionEntity permissionEntity = new PermissionEntity(permissionCode);
        RoleEntity roleEntity = new RoleEntity(roleName, activeRole);
        roleEntity.addPermission(permissionEntity);

        UserEntity userEntity = new UserEntity(username, activeUser);
        userEntity.addRole(roleEntity);
        return userRepository.save(userEntity);
    }
}
