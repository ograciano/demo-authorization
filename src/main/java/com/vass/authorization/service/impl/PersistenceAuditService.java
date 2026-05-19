package com.vass.authorization.service.impl;

import com.vass.authorization.entity.AuditLogEntity;
import com.vass.authorization.repository.AuditLogRepository;
import com.vass.authorization.service.AuditService;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersistenceAuditService implements AuditService {

    private final AuditLogRepository auditLogRepository;

    public PersistenceAuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public void registerDeniedPermissionCheck(Long userId, String resource, String action) {
        AuditLogEntity auditLogEntity = new AuditLogEntity(userId, resource, action, false);
        auditLogRepository.save(auditLogEntity);
    }

    @Override
    @Transactional
    public void registerAdminPermissionReplacement(
        String actor,
        Long targetUserId,
        Set<String> beforePermissions,
        Set<String> afterPermissions
    ) {
        AuditLogEntity auditLogEntity = AuditLogEntity.forAdminPermissionReplacement(
            actor,
            targetUserId,
            beforePermissions,
            afterPermissions
        );
        auditLogRepository.save(auditLogEntity);
    }
}
