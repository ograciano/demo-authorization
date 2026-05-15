package com.vass.authorization.controller;

import com.vass.authorization.dto.AssignPermissionRequestDTO;
import com.vass.authorization.dto.AssignPermissionResponseDTO;
import com.vass.authorization.dto.UserPermissionsResponseDTO;
import com.vass.authorization.service.PermissionAssignmentService;
import com.vass.authorization.service.PermissionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionQueryService permissionQueryService;
    private final PermissionAssignmentService permissionAssignmentService;

    public PermissionController(
            PermissionQueryService permissionQueryService,
            PermissionAssignmentService permissionAssignmentService
    ) {
        this.permissionQueryService = permissionQueryService;
        this.permissionAssignmentService = permissionAssignmentService;
    }

    @Operation(
            summary = "Consultar permisos vigentes por usuario",
            description = "Endpoint interno para consumo de authentication-service durante construccion de JWT."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Permisos consultados",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserPermissionsResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "ConPermisos",
                                            value = """
                                                    {"userId":1,"permissions":["REPORT:DOWNLOAD"],"timestamp":"2026-05-14T18:00:00Z"}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "SinPermisos",
                                            value = """
                                                    {"userId":10,"permissions":[],"timestamp":"2026-05-14T18:00:00Z"}
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parametro invalido",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {"error":"Bad Request","message":"Request invalido"}
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {"error":"Not Found","message":"Usuario no encontrado"}
                                            """
                            )
                    )
            )
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserPermissionsResponseDTO> getUserPermissions(
            @PathVariable @Positive Long userId
    ) {
        return ResponseEntity.ok(permissionQueryService.getUserPermissions(userId));
    }

    @Operation(
            summary = "Asignar permiso inicial a usuario",
            description = "Endpoint interno idempotente para asignar un permiso a un usuario."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Permiso asignado o previamente asignado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AssignPermissionResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Assigned",
                                            value = """
                                                    {"userId":1,"permission":"REPORT:DOWNLOAD","status":"ASSIGNED","timestamp":"2026-05-14T18:00:00Z"}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "AlreadyAssigned",
                                            value = """
                                                    {"userId":1,"permission":"REPORT:DOWNLOAD","status":"ALREADY_ASSIGNED","timestamp":"2026-05-14T18:00:01Z"}
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request invalido o permiso fuera de catalogo",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {"error":"Bad Request","message":"Permiso invalido o fuera de catalogo"}
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {"error":"Not Found","message":"Usuario no encontrado"}
                                            """
                            )
                    )
            )
    })
    @PostMapping("/users/{userId}")
    public ResponseEntity<AssignPermissionResponseDTO> assignPermission(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody AssignPermissionRequestDTO request
    ) {
        return ResponseEntity.ok(permissionAssignmentService.assignPermission(userId, request.permission()));
    }
}
