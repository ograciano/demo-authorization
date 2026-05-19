package com.vass.authorization.api.controller;

import com.vass.authorization.api.dto.request.PermissionAssignmentRequest;
import com.vass.authorization.api.dto.response.PermissionAssignmentResponse;
import com.vass.authorization.api.dto.response.UserPermissionsResponse;
import com.vass.authorization.api.error.ApiErrorResponse;
import com.vass.authorization.service.UserPermissionAssignmentService;
import com.vass.authorization.service.UserPermissionsQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/permissions")
@Tag(name = "Permissions", description = "Internal permissions query operations")
public class PermissionsController {

    private final UserPermissionsQueryService userPermissionsQueryService;
    private final UserPermissionAssignmentService userPermissionAssignmentService;

    public PermissionsController(
        UserPermissionsQueryService userPermissionsQueryService,
        UserPermissionAssignmentService userPermissionAssignmentService
    ) {
        this.userPermissionsQueryService = userPermissionsQueryService;
        this.userPermissionAssignmentService = userPermissionAssignmentService;
    }

    @GetMapping("/users/{userId}")
    @Operation(
        summary = "Get dynamic permissions by user",
        description = "Returns userId, permissions and timestamp for internal service consumption",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Permissions resolved",
                content = @Content(schema = @Schema(implementation = UserPermissionsResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid userId",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
        }
    )
    public ResponseEntity<UserPermissionsResponse> getPermissionsByUserId(
        @PathVariable @Positive(message = "userId must be greater than 0") Long userId
    ) {
        UserPermissionsResponse response = userPermissionsQueryService.getUserPermissions(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{userId}")
    @Operation(
        summary = "Assign default permission to user",
        description = "Assigns a permission idempotently and returns ASSIGNED or ALREADY_ASSIGNED",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Assignment processed",
                content = @Content(schema = @Schema(implementation = PermissionAssignmentResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid userId or permission",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
        }
    )
    public ResponseEntity<PermissionAssignmentResponse> assignPermissionToUser(
        @PathVariable @Positive(message = "userId must be greater than 0") Long userId,
        @Valid @RequestBody PermissionAssignmentRequest request
    ) {
        PermissionAssignmentResponse response = userPermissionAssignmentService.assignPermission(
            userId,
            request.permission()
        );
        return ResponseEntity.ok(response);
    }
}
