package com.vass.authorization.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vass.authorization.entity.PermissionEntity;
import com.vass.authorization.entity.RoleEntity;
import com.vass.authorization.entity.UserEntity;
import com.vass.authorization.repository.PermissionRepository;
import com.vass.authorization.repository.RoleRepository;
import com.vass.authorization.repository.UserRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    @Test
    void testCheckPermission_UserWithPermission_ReturnsAllowed() throws Exception {
        UserEntity user = createUserWithPermission(true, "REPORT:DOWNLOAD");

        String requestBody = """
                {"userId":%d,"resource":"report","action":"download"}
                """.formatted(user.getId());

        mockMvc.perform(post("/api/authorization/check-permission")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true))
                .andExpect(jsonPath("$.resource").value("REPORT"))
                .andExpect(jsonPath("$.action").value("DOWNLOAD"));
    }

    @Test
    void testCheckPermission_UserWithoutPermission_ReturnsDenied() throws Exception {
        UserEntity user = createUserWithPermission(true, "REPORT:READ");

        String requestBody = """
                {"userId":%d,"resource":"REPORT","action":"DOWNLOAD"}
                """.formatted(user.getId());

        mockMvc.perform(post("/api/authorization/check-permission")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.reason").value("Permiso denegado"));
    }

    @Test
    void testCheckPermission_InactiveUser_ReturnsDenied() throws Exception {
        UserEntity user = createUserWithPermission(false, "REPORT:DOWNLOAD");

        String requestBody = """
                {"userId":%d,"resource":"REPORT","action":"DOWNLOAD"}
                """.formatted(user.getId());

        mockMvc.perform(post("/api/authorization/check-permission")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.reason").value("Usuario inactivo"));
    }

    @Test
    void testCheckPermission_InvalidRequest_ReturnsBadRequest() throws Exception {
        String requestBody = """
                {"userId":-1,"resource":"","action":"DOWNLOAD"}
                """;

        mockMvc.perform(post("/api/authorization/check-permission")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    private UserEntity createUserWithPermission(boolean active, String permissionCode) {
        PermissionEntity permission = permissionRepository.save(new PermissionEntity(permissionCode));

        RoleEntity role = new RoleEntity("ROLE_" + permissionCode.replace(':', '_'));
        role.setPermissions(Set.of(permission));
        roleRepository.save(role);

        UserEntity user = new UserEntity(active);
        user.setRoles(Set.of(role));
        return userRepository.save(user);
    }
}
