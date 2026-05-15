package com.vass.authorization.controller;

import com.vass.authorization.dto.AuthorizationResponseDTO;
import com.vass.authorization.dto.CheckPermissionRequestDTO;
import com.vass.authorization.service.AuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authorization")
@SecurityRequirements
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    public AuthorizationController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Operation(
            summary = "Evaluar permiso de usuario",
            description = "Evalua autorizacion por userId, resource y action sin autenticar al usuario."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Evaluacion ejecutada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthorizationResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Permitido",
                                            value = """
                                                    {"allowed":true,"userId":1,"resource":"REPORT","action":"DOWNLOAD","reason":"Permiso concedido"}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Denegado",
                                            value = """
                                                    {"allowed":false,"userId":2,"resource":"REPORT","action":"DELETE","reason":"Permiso denegado"}
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request invalido",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    value = """
                                            {"error":"Bad Request","message":"Request invalido","details":{"userId":"must be greater than 0"}}
                                            """
                            )
                    )
            )
    })
    @PostMapping("/check-permission")
    public ResponseEntity<AuthorizationResponseDTO> checkPermission(
            @Valid @RequestBody CheckPermissionRequestDTO request
    ) {
        return ResponseEntity.ok(authorizationService.checkPermission(request));
    }
}
