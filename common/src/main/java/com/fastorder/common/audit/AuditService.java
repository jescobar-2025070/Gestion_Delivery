package com.fastorder.common.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Servicio de auditoría compartido por todos los microservicios.
 * Persiste con REQUIRES_NEW para no contaminar la transacción principal.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Long actorId, String actorEmail,
                          String entidad, Long entidadId,
                          String operacion, String detalle) {
        AuditLog log = AuditLog.builder()
                .actorId(actorId)
                .actorEmail(actorEmail)
                .entidad(entidad)
                .entidadId(entidadId)
                .operacion(operacion)
                .detalle(detalle)
                .timestamp(Instant.now())
                .build();
        auditLogRepository.save(log);
    }
}
