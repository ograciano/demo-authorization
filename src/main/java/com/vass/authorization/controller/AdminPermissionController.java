package com.vass.authorization.controller;

import com.vass.authorization.dto.AdminReplacePermissionsRequestDTO;
import com.vass.authorization.dto.AdminReplacePermissionsResponseDTO;
import com.vass.authorization.service.AdminPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/users")
public class AdminPermissionController {

    private final AdminPermissionService adminPermissionService;

    public AdminPermissionController(AdminPermissionService adminPermissionService) {
        this.adminPermissionService = adminPermissionService;
    }

    @Operation(
            summary = "Reemplazar permisos de usuario (ADMIN)",
            description = "Operacion administrativa atomica para reemplazar el conjunto completo de permisos.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Permisos reemplazados",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdminReplacePermissionsResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {"userId":10,"permissions":["REPORT:DOWNLOAD"],"status":"UPDATED","timestamp":"2026-05-14T20:00:00Z"}
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Request invalido o permisos no permitidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin privilegio ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{userId}/permissions")
    public ResponseEntity<AdminReplacePermissionsResponseDTO> replacePermissions(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody AdminReplacePermissionsRequestDTO request
    ) {
        return ResponseEntity.ok(adminPermissionService.replacePermissions(userId, request.permissions()));
    }
}
