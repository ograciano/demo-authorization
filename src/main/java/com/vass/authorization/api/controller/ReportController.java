package com.vass.authorization.api.controller;

import com.vass.authorization.api.error.ApiErrorResponse;
import com.vass.authorization.service.ReportDownloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Protected report operations")
public class ReportController {

    private final ReportDownloadService reportDownloadService;

    public ReportController(ReportDownloadService reportDownloadService) {
        this.reportDownloadService = reportDownloadService;
    }

    @GetMapping("/{reportId}/download")
    @Operation(
        summary = "Download report",
        description = "Requires JWT authentication and REPORT:DOWNLOAD permission",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Report content"),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
        }
    )
    public ResponseEntity<byte[]> downloadReport(@PathVariable @Positive Long reportId) {
        byte[] content = reportDownloadService.downloadReport(reportId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-" + reportId + ".txt")
            .contentType(MediaType.TEXT_PLAIN)
            .body(content);
    }
}
