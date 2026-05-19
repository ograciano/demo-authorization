package com.vass.authorization.integration;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vass.authorization.service.ReportDownloadService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ReportSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.security.jwt-secret}")
    private String jwtSecret;

    @MockBean
    private ReportDownloadService reportDownloadService;

    @Test
    void should_return_200_when_token_is_valid_and_permission_exists() throws Exception {
        when(reportDownloadService.downloadReport(1L)).thenReturn("ok".getBytes(StandardCharsets.UTF_8));

        String token = createToken("user-1", Set.of("REPORT:DOWNLOAD"), Instant.now().plusSeconds(600));

        mockMvc.perform(
                get("/api/reports/1/download")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            )
            .andExpect(status().isOk());

        verify(reportDownloadService).downloadReport(1L);
    }

    @Test
    void should_return_401_when_token_is_missing() throws Exception {
        mockMvc.perform(get("/api/reports/1/download"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.path").value("/api/reports/1/download"));

        verifyNoInteractions(reportDownloadService);
    }

    @Test
    void should_return_401_when_token_is_malformed() throws Exception {
        mockMvc.perform(
                get("/api/reports/1/download")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer malformed.token")
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.path").value("/api/reports/1/download"));

        verifyNoInteractions(reportDownloadService);
    }

    @Test
    void should_return_401_when_token_is_expired() throws Exception {
        String expiredToken = createToken("user-1", Set.of("REPORT:DOWNLOAD"), Instant.now().minusSeconds(60));

        mockMvc.perform(
                get("/api/reports/1/download")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.path").value("/api/reports/1/download"));

        verifyNoInteractions(reportDownloadService);
    }

    @Test
    void should_return_403_when_token_is_valid_but_permission_missing() throws Exception {
        String tokenWithoutPermission = createToken("user-2", Set.of("REPORT:READ"), Instant.now().plusSeconds(600));

        mockMvc.perform(
                get("/api/reports/1/download")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithoutPermission)
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.path").value("/api/reports/1/download"));

        verify(reportDownloadService, never()).downloadReport(anyLong());
    }

    private String createToken(String subject, Set<String> permissions, Instant expiration) throws Exception {
        String headerJson = objectMapper.writeValueAsString(Map.of("alg", "HS256", "typ", "JWT"));
        String payloadJson = objectMapper.writeValueAsString(
            Map.of(
                "sub",
                subject,
                "permissions",
                permissions,
                "exp",
                expiration.getEpochSecond()
            )
        );

        String encodedHeader = encodeSection(headerJson);
        String encodedPayload = encodeSection(payloadJson);
        String signature = sign(encodedHeader + "." + encodedPayload);
        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
    }

    private String encodeSection(String json) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}
