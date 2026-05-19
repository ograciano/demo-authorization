package com.vass.authorization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Table(name = "audit_log")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "resource", nullable = false, length = 80)
    private String resource;

    @Column(name = "action", nullable = false, length = 80)
    private String action;

    @Column(name = "allowed", nullable = false)
    private boolean allowed;

    @Column(name = "actor", length = 120)
    private String actor;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "before_permissions", length = 1200)
    private String beforePermissions;

    @Column(name = "after_permissions", length = 1200)
    private String afterPermissions;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AuditLogEntity() {
    }

    public AuditLogEntity(Long userId, String resource, String action, boolean allowed) {
        this.userId = userId;
        this.resource = resource;
        this.action = action;
        this.allowed = allowed;
    }

    public static AuditLogEntity forAdminPermissionReplacement(
        String actor,
        Long targetUserId,
        Set<String> beforePermissions,
        Set<String> afterPermissions
    ) {
        AuditLogEntity auditLogEntity = new AuditLogEntity(targetUserId, "USER_PERMISSIONS", "REPLACE", true);
        auditLogEntity.actor = actor;
        auditLogEntity.targetUserId = targetUserId;
        auditLogEntity.beforePermissions = joinPermissions(beforePermissions);
        auditLogEntity.afterPermissions = joinPermissions(afterPermissions);
        return auditLogEntity;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getResource() {
        return resource;
    }

    public String getAction() {
        return action;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getActor() {
        return actor;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public String getBeforePermissions() {
        return beforePermissions;
    }

    public String getAfterPermissions() {
        return afterPermissions;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private static String joinPermissions(Set<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "";
        }
        return String.join(",", new TreeSet<>(permissions));
    }
}
