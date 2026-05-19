package com.vass.authorization.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vass.authorization.entity.PermissionEntity;
import com.vass.authorization.entity.RoleEntity;
import com.vass.authorization.entity.UserEntity;
import com.vass.authorization.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PermissionsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void should_return_200_with_permissions_when_user_exists_with_permissions() throws Exception {
        UserEntity user = createUserWithPermission(
            "permissions-user-with-data",
            true,
            "ROLE_PERMISSIONS_WITH_DATA",
            true,
            "INTERNAL:PERMISSIONS_READ"
        );

        mockMvc.perform(get("/api/permissions/users/" + user.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(user.getId()))
            .andExpect(jsonPath("$.permissions[0]").value("INTERNAL:PERMISSIONS_READ"))
            .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void should_return_200_with_empty_permissions_when_user_exists_without_permissions() throws Exception {
        UserEntity user = userRepository.save(new UserEntity("permissions-user-empty", true));

        mockMvc.perform(get("/api/permissions/users/" + user.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(user.getId()))
            .andExpect(jsonPath("$.permissions").isArray())
            .andExpect(jsonPath("$.permissions").isEmpty())
            .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void should_return_404_when_user_does_not_exist() throws Exception {
        mockMvc.perform(get("/api/permissions/users/999999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.path").value("/api/permissions/users/999999"));
    }

    @Test
    void should_return_400_when_user_id_is_invalid() throws Exception {
        mockMvc.perform(get("/api/permissions/users/0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.path").value("/api/permissions/users/0"));
    }

    @Test
    void should_return_200_assigned_when_permission_is_new_for_user() throws Exception {
        UserEntity user = userRepository.save(new UserEntity("permissions-assigned-user", true));

        mockMvc.perform(
                post("/api/permissions/users/" + user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "permission": "REPORT:READ"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(user.getId()))
            .andExpect(jsonPath("$.permission").value("REPORT:READ"))
            .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    void should_return_200_already_assigned_when_permission_exists_for_user() throws Exception {
        UserEntity user = userRepository.save(new UserEntity("permissions-already-assigned-user", true));

        mockMvc.perform(
                post("/api/permissions/users/" + user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "permission": "REPORT:READ"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ASSIGNED"));

        mockMvc.perform(
                post("/api/permissions/users/" + user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "permission": "REPORT:READ"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(user.getId()))
            .andExpect(jsonPath("$.permission").value("REPORT:READ"))
            .andExpect(jsonPath("$.status").value("ALREADY_ASSIGNED"));
    }

    @Test
    void should_return_400_when_permission_is_invalid_or_blank() throws Exception {
        UserEntity user = userRepository.save(new UserEntity("permissions-invalid-input-user", true));

        mockMvc.perform(
                post("/api/permissions/users/" + user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "permission": "UNKNOWN:PERMISSION"
                        }
                        """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.path").value("/api/permissions/users/" + user.getId()));

        mockMvc.perform(
                post("/api/permissions/users/" + user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "permission": ""
                        }
                        """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void should_return_404_when_assigning_permission_to_unknown_user() throws Exception {
        mockMvc.perform(
                post("/api/permissions/users/999999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "permission": "REPORT:READ"
                        }
                        """)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.path").value("/api/permissions/users/999999"));
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
