package com.vass.authorization.api.controller;

import com.vass.authorization.api.dto.request.AdminPermissionReplacementRequest;
import com.vass.authorization.api.dto.response.AdminPermissionReplacementResponse;
import com.vass.authorization.api.error.ApiErrorResponse;
import com.vass.authorization.service.AdminPermissionReplacementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin Permissions", description = "Administrative operations for user direct permissions")
public class AdminPermissionsController {

    private final AdminPermissionReplacementService adminPermissionReplacementService;

    public AdminPermissionsController(AdminPermissionReplacementService adminPermissionReplacementService) {
        this.adminPermissionReplacementService = adminPermissionReplacementService;
    }

    @PutMapping("/{userId}/permissions")
    @Operation(
        summary = "Replace user direct permissions",
        description = "Replaces the full direct permissions set of the target user",
        security = {@SecurityRequirement(name = "bearerAuth")},
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Permissions replaced successfully",
                content = @Content(schema = @Schema(implementation = AdminPermissionReplacementResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid userId or permissions payload",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT token",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Authenticated user without ADMIN privileges",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Target user not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
        }
    )
    public ResponseEntity<AdminPermissionReplacementResponse> replacePermissions(
        @PathVariable @Positive(message = "userId must be greater than 0") Long userId,
        @Valid @RequestBody AdminPermissionReplacementRequest request,
        Authentication authentication
    ) {
        String actor = authentication == null ? "unknown" : authentication.getName();
        AdminPermissionReplacementResponse response = adminPermissionReplacementService.replacePermissions(
            userId,
            request.permissions(),
            actor
        );
        return ResponseEntity.ok(response);
    }
}
