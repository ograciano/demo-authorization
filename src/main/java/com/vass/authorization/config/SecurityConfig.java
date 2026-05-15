package com.vass.authorization.config;

import com.vass.authorization.security.JwtAuthenticationFilter;
import com.vass.authorization.security.PermissionAuthorizationService;
import com.vass.authorization.security.RestAccessDeniedHandler;
import com.vass.authorization.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final PermissionAuthorizationService permissionAuthorizationService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler,
            PermissionAuthorizationService permissionAuthorizationService
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/authorization/check-permission",
                                "/api/permissions/users/*",
                                "/h2-console/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        )
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reports/*/download")
                        .access((authentication, context) -> new AuthorizationDecision(
                                permissionAuthorizationService.hasPermission(authentication.get(), "REPORT:DOWNLOAD")
                        ))
                        .requestMatchers(HttpMethod.PUT, "/api/admin/users/*/permissions")
                        .access((authentication, context) -> new AuthorizationDecision(
                                permissionAuthorizationService.hasPermission(
                                        authentication.get(),
                                        "ADMIN:MANAGE_PERMISSIONS"
                                )
                        ))
                        .anyRequest()
                        .authenticated())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
