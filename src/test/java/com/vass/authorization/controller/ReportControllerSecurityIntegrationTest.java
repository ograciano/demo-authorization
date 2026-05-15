package com.vass.authorization.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vass.authorization.service.ReportService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ReportControllerSecurityIntegrationTest.TestConfig.class)
class ReportControllerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CountingReportService reportService;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Test
    void testDownloadReport_WithValidTokenAndPermission_ReturnsOk() throws Exception {
        String token = token("user-1", List.of("REPORT:DOWNLOAD"), Instant.now().plusSeconds(3600));

        mockMvc.perform(get("/api/reports/1/download")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testDownloadReport_WithoutToken_ReturnsUnauthorizedAndSkipsBusinessLogic() throws Exception {
        int before = reportService.invocations();

        mockMvc.perform(get("/api/reports/1/download"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        assertEquals(before, reportService.invocations());
    }

    @Test
    void testDownloadReport_WithInvalidToken_ReturnsUnauthorizedAndSkipsBusinessLogic() throws Exception {
        int before = reportService.invocations();

        mockMvc.perform(get("/api/reports/1/download")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        assertEquals(before, reportService.invocations());
    }

    @Test
    void testDownloadReport_WithMalformedAuthorizationHeader_ReturnsUnauthorized() throws Exception {
        int before = reportService.invocations();

        mockMvc.perform(get("/api/reports/1/download")
                        .header(HttpHeaders.AUTHORIZATION, "Token any-value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        assertEquals(before, reportService.invocations());
    }

    @Test
    void testDownloadReport_WithoutPermission_ReturnsForbiddenAndSkipsBusinessLogic() throws Exception {
        int before = reportService.invocations();
        String token = token("user-2", List.of("REPORT:READ"), Instant.now().plusSeconds(3600));

        mockMvc.perform(get("/api/reports/1/download")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));

        assertEquals(before, reportService.invocations());
    }

    @Test
    void testDownloadReport_WithPermissionAndMissingReport_ReturnsNotFound() throws Exception {
        String token = token("user-1", List.of("REPORT:DOWNLOAD"), Instant.now().plusSeconds(3600));

        mockMvc.perform(get("/api/reports/999/download")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    private String token(String subject, List<String> permissions, Instant expiration) {
        return Jwts.builder()
                .subject(subject)
                .claim("permissions", permissions)
                .expiration(Date.from(expiration))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        CountingReportService countingReportService() {
            return new CountingReportService();
        }
    }

    static class CountingReportService extends ReportService {
        private final AtomicInteger invocations = new AtomicInteger();

        @Override
        public String downloadReport(Long reportId) {
            invocations.incrementAndGet();
            return super.downloadReport(reportId);
        }

        int invocations() {
            return invocations.get();
        }
    }
}
