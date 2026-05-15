package com.vass.authorization.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

class JwtAuthenticationFilterTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilter_MalformedHeader_TriggersEntryPoint() throws ServletException, IOException {
        StubJwtService jwtService = new StubJwtService();
        CountingEntryPoint entryPoint = new CountingEntryPoint();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, entryPoint);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/reports/1/download");
        request.addHeader("Authorization", "Token abc");
        MockHttpServletResponse response = new MockHttpServletResponse();
        TrackingFilterChain chain = new TrackingFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(1, entryPoint.invocations);
        assertTrue(!chain.called);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilter_ValidBearer_SetsAuthentication() throws ServletException, IOException {
        StubJwtService jwtService = new StubJwtService();
        jwtService.authentication = new UsernamePasswordAuthenticationToken(
                "user-1",
                null,
                List.of(new SimpleGrantedAuthority("PERM_REPORT:DOWNLOAD"))
        );
        CountingEntryPoint entryPoint = new CountingEntryPoint();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, entryPoint);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/reports/1/download");
        request.addHeader("Authorization", "Bearer good-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        TrackingFilterChain chain = new TrackingFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(0, entryPoint.invocations);
        assertTrue(chain.called);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilter_InvalidToken_TriggersEntryPoint() throws ServletException, IOException {
        StubJwtService jwtService = new StubJwtService();
        jwtService.throwJwtException = true;
        CountingEntryPoint entryPoint = new CountingEntryPoint();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, entryPoint);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/reports/1/download");
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        TrackingFilterChain chain = new TrackingFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(1, entryPoint.invocations);
        assertTrue(!chain.called);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private static class StubJwtService extends JwtService {
        private boolean throwJwtException;
        private org.springframework.security.core.Authentication authentication;

        StubJwtService() {
            super("authorization-service-jwt-secret-key-2026-demo");
        }

        @Override
        public org.springframework.security.core.Authentication toAuthentication(String token) {
            if (throwJwtException) {
                throw new JwtException("invalid");
            }
            return authentication;
        }
    }

    private static class CountingEntryPoint implements AuthenticationEntryPoint {
        private int invocations;

        @Override
        public void commence(
                HttpServletRequest request,
                HttpServletResponse response,
                org.springframework.security.core.AuthenticationException authException
        ) {
            this.invocations++;
            if (!(authException instanceof BadCredentialsException)) {
                throw new IllegalStateException("Expected BadCredentialsException");
            }
        }
    }

    private static class TrackingFilterChain implements FilterChain {
        private boolean called;

        @Override
        public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) {
            this.called = true;
        }
    }
}
