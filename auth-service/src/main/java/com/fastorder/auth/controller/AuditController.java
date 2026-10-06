package com.fastorder.auth.controller;

import com.fastorder.common.audit.AuditLog;
import com.fastorder.common.audit.AuditLogRepository;
import com.fastorder.common.domain.AppConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.API_PREFIX + "/auditoria")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditLog> getAuditorias(
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) Long actorId,
            Pageable pageable) {
        if (entidad != null) {
            return auditLogRepository.findByEntidad(entidad, pageable);
        } else if (actorId != null) {
            return auditLogRepository.findByActorId(actorId, pageable);
        }
        return auditLogRepository.findAll(pageable);
    }
}
