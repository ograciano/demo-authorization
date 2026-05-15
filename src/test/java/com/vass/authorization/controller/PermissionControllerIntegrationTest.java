package com.vass.authorization.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class PermissionControllerIntegrationTest {

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
        seedPermissionCatalog();
    }

    @Test
    void testGetUserPermissions_UserWithPermissions_ReturnsOkAndContract() throws Exception {
        UserEntity user = createUserWithPermissions("report:download", "REPORT:READ");

        mockMvc.perform(get("/api/permissions/users/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.permissions[0]").value("REPORT:DOWNLOAD"))
                .andExpect(jsonPath("$.permissions[1]").value("REPORT:READ"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testGetUserPermissions_UserWithoutPermissions_ReturnsEmptyList() throws Exception {
        UserEntity user = userRepository.save(new UserEntity(true));

        mockMvc.perform(get("/api/permissions/users/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.permissions").isArray())
                .andExpect(jsonPath("$.permissions").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testGetUserPermissions_InvalidUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/permissions/users/{userId}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void testGetUserPermissions_UserNotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/permissions/users/{userId}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }

    @Test
    void testAssignPermission_NewAssignment_ReturnsAssigned() throws Exception {
        UserEntity user = userRepository.save(new UserEntity(true));

        mockMvc.perform(post("/api/permissions/users/{userId}", user.getId())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"permission":"REPORT:DOWNLOAD"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.permission").value("REPORT:DOWNLOAD"))
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testAssignPermission_AlreadyAssigned_ReturnsIdempotentStatus() throws Exception {
        UserEntity user = userRepository.save(new UserEntity(true));

        mockMvc.perform(post("/api/permissions/users/{userId}", user.getId())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"permission":"REPORT:DOWNLOAD"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        mockMvc.perform(post("/api/permissions/users/{userId}", user.getId())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"permission":"REPORT:DOWNLOAD"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ALREADY_ASSIGNED"));
    }

    @Test
    void testAssignPermission_InvalidPermission_ReturnsBadRequest() throws Exception {
        UserEntity user = userRepository.save(new UserEntity(true));

        mockMvc.perform(post("/api/permissions/users/{userId}", user.getId())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"permission":"INVALID:VALUE"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void testAssignPermission_InvalidUserId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/permissions/users/{userId}", 0)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"permission":"REPORT:DOWNLOAD"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void testAssignPermission_UserNotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/permissions/users/{userId}", 99999L)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"permission":"REPORT:DOWNLOAD"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    private UserEntity createUserWithPermissions(String... permissionCodes) {
        PermissionEntity firstPermission = permissionRepository.findByCodeIgnoreCase(permissionCodes[0])
                .orElseGet(() -> permissionRepository.save(new PermissionEntity(permissionCodes[0])));
        PermissionEntity secondPermission = permissionRepository.findByCodeIgnoreCase(permissionCodes[1])
                .orElseGet(() -> permissionRepository.save(new PermissionEntity(permissionCodes[1])));

        RoleEntity role = new RoleEntity("DYNAMIC_ROLE_" + permissionCodes[0].replace(':', '_'));
        role.setPermissions(Set.of(
                firstPermission,
                secondPermission
        ));
        roleRepository.save(role);

        UserEntity user = new UserEntity(true);
        user.setRoles(Set.of(role));
        return userRepository.save(user);
    }

    private void seedPermissionCatalog() {
        PermissionEntity reportDownload = permissionRepository.save(new PermissionEntity("REPORT:DOWNLOAD"));
        PermissionEntity reportRead = permissionRepository.save(new PermissionEntity("REPORT:READ"));

        RoleEntity reportDownloader = new RoleEntity("REPORT_DOWNLOADER");
        reportDownloader.getPermissions().add(reportDownload);
        roleRepository.save(reportDownloader);

        RoleEntity reportReader = new RoleEntity("REPORT_READER");
        reportReader.getPermissions().add(reportRead);
        roleRepository.save(reportReader);
    }
}
