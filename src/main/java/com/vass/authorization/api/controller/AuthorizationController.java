package com.vass.authorization.api.controller;

import com.vass.authorization.api.dto.request.PermissionCheckRequest;
import com.vass.authorization.api.dto.response.PermissionCheckResponse;
import com.vass.authorization.api.error.ApiErrorResponse;
import com.vass.authorization.service.AuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authorization")
@Tag(name = "Authorization", description = "Permission checks for protected actions")
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    public AuthorizationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping("/check-permission")
    @Operation(
        summary = "Validate permission by user roles",
        description = "Returns allowed=true/false based on effective role permissions",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Permission evaluated",
                content = @Content(schema = @Schema(implementation = PermissionCheckResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
        }
    )
    public ResponseEntity<PermissionCheckResponse> checkPermission(
        @Valid @RequestBody PermissionCheckRequest request
    ) {
        boolean allowed = authorizationService.checkPermission(request);
        return ResponseEntity.ok(new PermissionCheckResponse(allowed));
    }
}
